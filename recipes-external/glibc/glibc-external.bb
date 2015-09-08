SRC_URI = "\
    file://SUPPORTED \
    file://nscd.init;subdir=${REL_S}/nscd \
    file://nscd.conf;subdir=${REL_S}/nscd \
    file://nscd.service;subdir=${REL_S}/nscd \
"
REL_S = "${@os.path.relpath('${S}', '${WORKDIR}')}"

require recipes-core/glibc/glibc-common.inc
inherit external-toolchain
require recipes-external/glibc/glibc-external-version.inc

DEPENDS += "virtual/${TARGET_PREFIX}binutils \
            linux-libc-headers"
PROVIDES += "glibc \
             virtual/${TARGET_PREFIX}libc-for-gcc \
             virtual/${TARGET_PREFIX}libc-initial \
             virtual/libc \
             virtual/libintl \
             virtual/libiconv"

def get_external_libc_license(d):
    errnosearch = os.path.join(d.getVar('includedir', True), 'errno.h')
    found = oe.external.find_sysroot_files([errnosearch], d)
    if found:
        errno_paths = found[0]
        if errno_paths:
            with open(errno_paths[0], 'rU') as f:
                text = f.read()

            lictext = """   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version."""

            if lictext in text:
                return 'LGPL-2.1+'

    return 'UNKNOWN'

LICENSE := "${@get_external_libc_license(d)}"

require recipes-external/glibc/glibc-sysroot-setup.inc
require recipes-external/glibc/glibc-package-adjusted.inc

FILES_MIRRORS .= "\
    ${base_sbindir}/|/usr/bin/ \n\
    ${base_sbindir}/|/usr/${baselib}/bin/ \n\
    ${sbindir}/|/usr/bin/ \n\
    ${sbindir}/|/usr/${baselib}/bin/ \n\
"

python do_install () {
    bb.build.exec_func('external_toolchain_do_install', d)
    bb.build.exec_func('glibc_external_do_install_extra', d)
    bb.build.exec_func('adjust_locale_names', d)
}

python adjust_locale_names () {
    """Align locale charset names with glibc-locale expectations."""
    # Read in supported locales and associated encodings
    supported = {}
    with open(base_path_join(d.getVar('WORKDIR', True), "SUPPORTED")) as f:
        for line in f.readlines():
            try:
                locale, charset = line.rstrip().split()
            except ValueError:
                continue
            supported[locale] = charset

    # GLIBC_GENERATE_LOCALES var specifies which locales to be generated. empty or "all" means all locales
    to_generate = d.getVar('GLIBC_GENERATE_LOCALES', True)
    if not to_generate or to_generate == 'all':
        to_generate = supported.keys()
    else:
        to_generate = to_generate.split()
        for locale in to_generate:
            if locale not in supported:
                if '.' in locale:
                    charset = locale.split('.')[1]
                else:
                    charset = 'UTF-8'
                    bb.warn("Unsupported locale '%s', assuming encoding '%s'" % (locale, charset))
                supported[locale] = charset

    localedir = oe.path.join(d.getVar('D', True), d.getVar('localedir', True))
    for locale in to_generate:
        if '.' not in locale:
            continue

        locale, charset = locale.split('.', 1)
        if '-' not in charset:
            continue

        oe_name = locale + '.' + charset.lower()
        existing_name = locale + '.' + charset.lower().replace('-', '')
        this_localedir = oe.path.join(localedir, existing_name)
        if os.path.exists(this_localedir):
            bb.debug(1, '%s -> %s' % (this_localedir, oe.path.join(localedir, oe_name)))
            os.rename(this_localedir, oe.path.join(localedir, oe_name))
}

glibc_external_do_install_extra () {
    mkdir -p ${D}${sysconfdir}
    touch ${D}${sysconfdir}/ld.so.conf

    if [ ! -e ${D}${libdir}/libc.so ]; then
        bbfatal "Unable to locate installed libc.so file (${libdir}/libc.so)." \
                "This may mean that your external toolchain uses a different" \
                "multi-lib setup than your machine configuration"
    fi
    create_multilib_link ${D}
    if [ "${GLIBC_INTERNAL_USE_BINARY_LOCALE}" != "precompiled" ]; then
        rm -rf ${D}${localedir}
    fi

    # Work around localedef failures for non-precompiled
    for locale in bo_CN bo_IN; do
        sed -i -e '/^name_fmt\s/s/""/"???"/' "${D}${datadir}/i18n/locales/$locale"
        if grep -q '^name_fmt.*""' "${D}${datadir}/i18n/locales/$locale"; then
            bbfatal "sed did not fix $locale"
        fi
    done
}

EXTERNAL_EXTRA_FILES += "\
    ${bindir}/mtrace ${bindir}/xtrace ${bindir}/sotruss \
    ${datadir}/i18n \
    ${libdir}/gconv \
    ${@'${localedir}' if d.getVar('GLIBC_INTERNAL_USE_BINARY_LOCALE', True) == 'precompiled' else ''} \
"

# These files are picked up out of the sysroot by glibc-locale, so we don't
# need to keep them around ourselves.
do_install_locale_append() {
	rm -rf ${D}${localedir}
}

python () {
    # Undo the do_install_append which joined shell to python
    install = d.getVar('do_install', False)
    python, shell = install.split('rm -f ', 1)
    # bits/syscall.h is in linux-libc-headers-external
    shell = shell.replace('oe_multilib_header bits/syscall.h\n', '')
    d.setVar('do_install_glibc', 'rm -f ' + shell)
    d.setVarFlag('do_install_glibc', 'func', '1')
    new_install = python + '\n    bb.build.exec_func("do_install_glibc", d)\n'
    d.setVar('do_install', new_install.replace('\t', '    '))

    # Ensure that we pick up just libm, not all libs that start with m
    baselibs = d.getVar('libc_baselibs', False)
    baselibs.replace('${base_libdir}/libm*.so.*', '${base_libdir}/libm.so.*')
    d.setVar('libc_baselibs', baselibs)
}

# Default pattern is too greedy
FILES_${PN}-utils = "\
    ${bindir}/gencat \
    ${bindir}/getconf \
    ${bindir}/getent \
    ${bindir}/iconv \
    ${sbindir}/iconvconfig \
    ${bindir}/lddlibc4 \
    ${bindir}/locale \
    ${bindir}/makedb \
    ${bindir}/pcprofiledump \
    ${bindir}/pldd \
    ${bindir}/rpcgen \
    ${bindir}/sprof \
"
FILES_${PN}-doc += "${infodir}/libc.info*"

# Extract for use by do_install_locale
FILES_${PN} += "\
    ${bindir}/localedef \
    ${libdir}/gconv \
    ${libdir}/locale \
    ${datadir}/locale \
    ${datadir}/i18n \
"

FILES_${PN}-dev_remove := "${datadir}/aclocal"

FILES_${PN}-dev_remove = "/lib/*.o"
FILES_${PN}-dev += "${libdir}/*crt*.o"

libc_baselibs_dev += "${@' '.join('${libdir}/' + os.path.basename(l.replace('${SOLIBS}', '${SOLIBSDEV}')) for l in '${libc_baselibs}'.replace('${base_libdir}/ld*${SOLIBS}', '').split() if l.endswith('${SOLIBS}'))}"
FILES_${PN}-staticdev = "\
    ${@'${libc_baselibs_dev}'.replace('${SOLIBSDEV}', '.a')} \
    ${libdir}/libg.a \
    ${libdir}/libieee.a \
    ${libdir}/libmcheck.a \
    ${libdir}/librpcsvc.a \
"

FILES_${PN}-dev += "\
    ${libc_baselibs_dev} \
    ${libdir}/libcidn${SOLIBSDEV} \
    ${libdir}/libthread_db${SOLIBSDEV} \
    ${libdir}/libpthread${SOLIBSDEV} \
"
FILES_${PN}-dev += "${@sysroot_multilib_suffix(d)}"
libc_headers_file = "${@bb.utils.which('${FILESPATH}', 'libc.headers')}"
FILES_${PN}-dev += "\
    ${@' '.join('${includedir}/' + f.rstrip() for f in base_read_file('${libc_headers_file}').splitlines())} \
    ${includedir}/fpu_control.h \
    ${includedir}/stdc-predef.h \
    ${includedir}/uchar.h \
"
FILES_${PN}-dev[file-checksums] += "${libc_headers_file}"

# Currently, ldd and tzcode from Sourcery G++ still have #!/bin/bash
RDEPENDS_ldd += "bash"
RDEPENDS_tzcode += "bash"

# glibc's utils need libgcc
do_package[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_ipk[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_deb[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_rpm[depends] += "${MLPREFIX}libgcc:do_packagedata"
