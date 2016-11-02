import os.path
import oe.path
import bb


def run(d, cmd, *args):
    topdir = d.getVar('TOPDIR', True)
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)
    if toolchain_path:
        target_prefix = d.getVar('EXTERNAL_TARGET_SYS', True) + '-'
        path = os.path.join(toolchain_path, 'bin', target_prefix + cmd)
        args = [path] + list(args)

        try:
            output, _ = bb.process.run(args, cwd=topdir)
        except bb.process.CmdError as exc:
            bb.debug(1, str(exc))
        else:
            return output

    return 'UNKNOWN'


def get_file_search_metadata(d):
    '''Given the metadata, return the mirrors and sysroots to operate against.'''

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


def find_sysroot_files(paths, d):
    sysroots, mirrors = get_file_search_metadata(d)
    expanded = expand_paths(paths, mirrors)
    search_results = list(search_sysroots(expanded, sysroots))
    return [v for k, v in search_results]
