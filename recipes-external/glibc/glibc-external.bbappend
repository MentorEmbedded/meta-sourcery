DEPENDS_append_tcmode-external-sourcery = " linux-libc-headers"
FILES_${PN}-dev_remove_tcmode-external-sourcery = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

# glibc may need libssp for -fstack-protector builds
do_packagedata[depends] += "gcc-runtime:do_packagedata"
