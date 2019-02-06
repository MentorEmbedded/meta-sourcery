python () {
    depends = d.getVarFlag('do_package', 'depends', False)
    depends = depends.replace('virtual/${MLPREFIX}libc:do_packagedata', '')
    d.setVarFlag('do_package', 'depends', depends)
}

# gcc-runtime needs libc, but glibc's utilities need libssp in some cases, so
# short-circuit the interdependency here by manually specifying it rather than
# depending on the libc packagedata.
libc_rdep = "${@'${PREFERRED_PROVIDER_virtual/libc}' if '${PREFERRED_PROVIDER_virtual/libc}' else '${TCLIBC}'}"
RDEPENDS_libgomp += "${libc_rdep}"
RDEPENDS_libssp += "${libc_rdep}"
RDEPENDS_libstdc++ += "${libc_rdep}"
RDEPENDS_libatomic += "${libc_rdep}"
RDEPENDS_libquadmath += "${libc_rdep}"
RDEPENDS_libmpx += "${libc_rdep}"

do_package_write_ipk[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_deb[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_rpm[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
