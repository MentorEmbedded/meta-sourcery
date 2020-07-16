# Remove files provided by linux-libc-headers
linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"

do_install_append_tcmode-external-sourcery_class-target () {
    for d in ${linux_include_subdirs}; do
        rm -rf "${D}${includedir}/$d"
    done
}

RDEPENDS_${PN}-dev_append_tcmode-external-sourcery_class-target = " linux-libc-headers-dev"
