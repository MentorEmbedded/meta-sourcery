# Remove files provided by linux-libc-headers
linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"

do_install_append_tcmode-external-sourcery () {
    for d in ${linux_include_subdirs}; do
        rm -rf "${D}${includedir}/$d"
    done
}

RDEPENDS_${PN}-dev_append_tcmode-external-sourcery = " linux-libc-headers-dev"
