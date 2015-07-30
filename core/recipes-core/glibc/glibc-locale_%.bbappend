require recipes-external/glibc/glibc-external-version.inc

python () {
    if d.getVar('TCMODE', True).startswith('external-sourcery'):
        d.setVar('INHIBIT_PACKAGE_DEBUG_SPLIT', '1')
}
