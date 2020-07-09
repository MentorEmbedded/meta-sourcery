require recipes-kernel/linux-libc-headers/linux-libc-headers.inc
inherit external-toolchain

# Restore, overwritten by external-toolchain.bbclass
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "${COMMON_LIC_CHKSUM}"
DEPENDS = ""
SRC_URI = ""

linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"
FILES_${PN}-dev = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

BBCLASSEXTEND = ""

bberror_task-install () {
    # Silence any errors from oe_multilib_header, as we don't care about
    # missing multilib headers, as the oe-core glibc version isn't necessarily
    # the same as our own.
    :
}

do_install_armmultilib () {
    :
}
