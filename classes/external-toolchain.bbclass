# This class provides everything necessary for a recipe to pull bits from an
# external toolchain:
# - Automatically sets LIC_FILES_CHKSUM based on LICENSE if appropriate
# - Searches the external toolchain sysroot and alternate locations for the
#   patterns specified in the FILES variables, with support for checking
#   alternate locations within the sysroot as well
# - Automatically PROVIDES/RPROVIDES the non-external-suffixed names
# - Usual bits to handle packaging of existing binaries
# - Automatically skips the recipe if its files aren't available in the
#   external toolchain
# - Automatically grabs all the .debug files for everything included

# Since these are prebuilt binaries, there are no source files to checksum for
# LIC_FILES_CHKSUM, so use the license from common-licenses
inherit common-license

# We don't extract anything which will create S, and we don't want to see the
# warning about it
S = "${WORKDIR}"

# Prebuilt binaries, no need for any default dependencies
INHIBIT_DEFAULT_DEPS = "1"

EXTERNAL_PN ?= "${@PN.replace('-external', '')}"
PROVIDES += "${EXTERNAL_PN}"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "${COMMON_LIC_CHKSUM}"

EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@external_run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"
EXTERNAL_TOOLCHAIN_LIBROOT ?= "${@external_run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-file-name=crtbegin.o'])).rstrip().replace('/crtbegin.o', '')}"

EXTERNAL_INSTALL_SOURCE_PATHS = "\
    ${EXTERNAL_TOOLCHAIN_SYSROOT} \
    ${EXTERNAL_TOOLCHAIN}/${EXTERNAL_TARGET_SYS} \
    ${EXTERNAL_TOOLCHAIN_SYSROOT}/.. \
    ${EXTERNAL_TOOLCHAIN} \
    ${D} \
"

# Potential locations within the external toolchain sysroot
FILES_MIRRORS = "\
    ${bindir}/|/usr/${baselib}/bin/\n \
    ${base_libdir}/|/usr/${baselib}/\n \
    ${libexecdir}/|/usr/libexec/\n \
    ${libexecdir}/|/usr/${baselib}/${PN}\n \
    ${mandir}/|/usr/share/man/\n \
    ${mandir}/|/usr/man/\n \
    ${mandir}/|/man/\n \
    ${mandir}/|/share/doc/*-${EXTERNAL_TARGET_SYS}/man/\n \
    ${prefix}/|${base_prefix}/\n \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

EXTERNAL_PV_PREFIX ?= ""
EXTERNAL_PV_SUFFIX ?= ""
PV_prepend = "${@'${EXTERNAL_PV_PREFIX}' if '${EXTERNAL_PV_PREFIX}' else ''}"
PV_append = "${@'${EXTERNAL_PV_SUFFIX}' if '${EXTERNAL_PV_SUFFIX}' else ''}"

EXTERNAL_EXTRA_FILES ?= ""

# Skip this recipe if we don't have files in the external toolchain
EXTERNAL_AUTO_PROVIDE ?= "0"
EXTERNAL_AUTO_PROVIDE[type] = "boolean"
EXTERNAL_AUTO_PROVIDE_class-target ?= "1"

# We don't care if this path references other variables
EXTERNAL_TOOLCHAIN[vardepvalue] = "${EXTERNAL_TOOLCHAIN}"

# We don't want to rebuild if the path to the toolchain changes, only if the
# toolchain changes
external_toolchain_do_install[vardepsexclude] += "EXTERNAL_TOOLCHAIN"
EXTERNAL_INSTALL_SOURCE_PATHS[vardepsexclude] += "EXTERNAL_TOOLCHAIN"

python () {
    # Skipping only matters up front
    if d.getVar('BB_WORKERCONTEXT', True) == '1':
        return

    # We're not an available provider if there's no external toolchain
    if not d.getVar("EXTERNAL_TOOLCHAIN"):
        raise bb.parse.SkipPackage("External toolchain not configured (EXTERNAL_TOOLCHAIN not set).")

    if not oe.data.typed_value('EXTERNAL_AUTO_PROVIDE', d):
        return

    sysroots, mirrors = get_file_search_metadata(d)
    pattern = d.getVar('EXTERNAL_PROVIDE_PATTERN', True)
    if pattern is None:
        files = list(gather_pkg_files(d))
        expanded = expand_paths(files, mirrors)
        paths = search_sysroots(expanded, sysroots)
        if not any(f for p, f in paths):
            raise bb.parse.SkipPackage('No files found in external toolchain sysroot for `{}`'.format(', '.join(files)))
    elif not pattern:
        return
    else:
        expanded = oe.external_toolchain.expand_paths([pattern], mirrors)
        paths = oe.external_toolchain.search_sysroots(expanded, sysroots)
        if not any(f for p, f in paths):
            raise bb.parse.SkipPackage('No files found in external toolchain sysroot for `{}`'.format(pattern))
}

python do_install () {
    bb.build.exec_func('external_toolchain_do_install', d)
    if 'do_install_extra' in d:
        bb.build.exec_func('do_install_extra', d)
}

python external_toolchain_do_install () {
    import subprocess
    installdest = d.getVar('D', True)
    sysroots, mirrors = get_file_search_metadata(d)
    files = gather_pkg_files(d)
    copy_from_sysroots(files, sysroots, mirrors, installdest)
    subprocess.check_call(['chown', '-R', 'root:root', installdest])
}
external_toolchain_do_install[vardeps] += "${@' '.join('FILES_%s' % pkg for pkg in '${PACKAGES}'.split())}"

def get_file_search_metadata(d):
    '''Given the metadata, return the mirrors and sysroots to operate against.'''
    from collections import defaultdict

    mirrors = []
    for entry in d.getVar('FILES_MIRRORS', True).replace('\\n', '\n').split('\n'):
        entry = entry.strip()
        if not entry:
            continue
        pattern, subst = entry.strip().split('|', 1)
        mirrors.append(('^' + pattern, subst))

    source_paths = [os.path.realpath(p)
                    for p in d.getVar('EXTERNAL_INSTALL_SOURCE_PATHS', True).split()]

    return source_paths, mirrors

def gather_pkg_files(d):
    '''Given the metadata, return all the files we want to copy to ${D} for
    this recipe.'''
    import itertools
    files = []
    for pkg in d.getVar('PACKAGES', True).split():
        files = itertools.chain(files, (d.getVar('FILES_{}'.format(pkg), True) or '').split())
    files = itertools.chain(files, d.getVar('EXTERNAL_EXTRA_FILES', True).split())
    return files

def copy_from_sysroots(pathnames, sysroots, mirrors, installdest):
    '''Copy the specified files from the specified sysroots, also checking the
    specified mirror patterns as alternate paths, to the specified destination.'''
    import subprocess

    expanded_pathnames = expand_paths(pathnames, mirrors)
    searched_paths = search_sysroots(expanded_pathnames, sysroots)
    for path, files in searched_paths:
        if not files:
            bb.debug(1, 'Failed to find `{}`'.format(path))
        else:
            destdir = oe.path.join(installdest, os.path.dirname(path))
            bb.utils.mkdirhier(destdir)
            subprocess.check_call(['cp', '-pPR'] + list(files) + [destdir + '/'])
            bb.note('Copied `{}`  to `{}/`'.format(', '.join(files), destdir))

def expand_paths(pathnames, mirrors):
    '''Apply search/replace to paths to get alternate search paths.

    Returns a generator with tuples of (pathname, expanded_paths).'''
    import re
    for pathname in pathnames:
        expanded_paths = [pathname]

        for search, replace in mirrors:
            try:
                new_pathname = re.sub(search, replace, pathname, count=1)
            except re.error as exc:
                bb.warn("Invalid pattern for `%s`" % search)
                continue
            if new_pathname != pathname:
                expanded_paths.append(new_pathname)

        yield pathname, expanded_paths

def search_sysroots(path_entries, sysroots):
    '''Search the supplied sysroots for the supplied paths, checking supplied
    alternate paths. Expects entries in the format (pathname, all_paths).

    Returns a generator with tuples of (pathname, found_paths).'''
    import glob
    import itertools
    for path, pathnames in path_entries:
        for sysroot, pathname in ((s, p) for s in sysroots
                                         for p in itertools.chain([path], pathnames)):
            check_path = sysroot + os.sep + pathname
            found_paths = glob.glob(check_path)
            if found_paths:
                yield path, found_paths
                break
        else:
            yield path, None

# Change do_install's CWD to EXTERNAL_TOOLCHAIN for convenience
do_install[dirs] = "${D} ${EXTERNAL_TOOLCHAIN}"

# Debug files are likely already split out
INHIBIT_PACKAGE_STRIP = "1"

# Toolchain shipped binaries weren't necessarily built ideally
WARN_QA_remove = "ldflags textrel"
ERROR_QA_remove = "ldflags textrel"

RPROVIDES_${PN} += "${EXTERNAL_PN}"
RPROVIDES_${PN}-dev += "${EXTERNAL_PN}-dev"
RPROVIDES_${PN}-staticdev += "${EXTERNAL_PN}-staticdev"
RPROVIDES_${PN}-dbg += "${EXTERNAL_PN}-dbg"
RPROVIDES_${PN}-doc += "${EXTERNAL_PN}-doc"
RPROVIDES_${PN}-locale += "${EXTERNAL_PN}-locale"
LOCALEBASEPN = "${EXTERNAL_PN}"

FILES_${PN} = ""
FILES_${PN}-dev = ""
FILES_${PN}-staticdev = ""
FILES_${PN}-doc = ""
FILES_${PN}-locale = ""

def debug_paths(d):
    l = d.createCopy()
    l.finalize()
    paths = []
    exclude = [
        l.getVar('datadir', True),
        l.getVar('includedir', True),
    ]
    for p in l.getVar('PACKAGES', True).split():
        if p.endswith('-dbg'):
            continue
        for f in (l.getVar('FILES_%s' % p, True) or '').split():
            if any((f == x or f.startswith(x + '/')) for x in exclude):
                continue
            d = os.path.dirname(f)
            b = os.path.basename(f)
            paths.append('/usr/lib/debug{0}/{1}.debug'.format(d, b))
            paths.append('{0}/.debug/{1}'.format(d, b))
            paths.append('{0}/.debug/{1}.debug'.format(d, b))
    return set(paths)

FILES_${PN}-dbg = "${@' '.join(debug_paths(d))}"

# do_package[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
# do_package_write_ipk[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
# do_package_write_deb[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
# do_package_write_rpm[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
