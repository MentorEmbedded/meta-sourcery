require recipes-kernel/linux-libc-headers/linux-libc-headers.inc
inherit external-toolchain

# Restore, overwritten by external-toolchain.bbclass
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "${COMMON_LIC_CHKSUM}"
DEPENDS = ""
SRC_URI = ""

linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound video"
FILES:${PN}-dev = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

libc_headers_file = "${@bb.utils.which('${BBPATH}', 'recipes-external/glibc/glibc-external/libc.headers')}"
FILES:${PN}-dev += "\
    ${@' '.join('${includedir}/' + f.rstrip() for f in oe.utils.read_file('${libc_headers_file}').splitlines() if f.startswith('sys/'))} \
"
FILES:${PN}-dev[file-checksums] += "${libc_headers_file}:True"

BBCLASSEXTEND = ""

bberror:task-install () {
    # Silence any errors from oe_multilib_header, as we don't care about
    # missing multilib headers, as the oe-core glibc version isn't necessarily
    # the same as our own.
    :
}

do_install_armmultilib () {
    :
}
