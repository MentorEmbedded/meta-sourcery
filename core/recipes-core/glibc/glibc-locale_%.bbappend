python () {
    if d.getVar('TCMODE', True).startswith('external-sourcery'):
        d.setVar('INHIBIT_PACKAGE_DEBUG_SPLIT', '1')
}

# localedef needs libgcc & libc
do_package[depends] += "${MLPREFIX}libgcc:do_packagedata virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_ipk[depends] += "${MLPREFIX}libgcc:do_packagedata virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_deb[depends] += "${MLPREFIX}libgcc:do_packagedata virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_rpm[depends] += "${MLPREFIX}libgcc:do_packagedata virtual/${MLPREFIX}libc:do_packagedata"
