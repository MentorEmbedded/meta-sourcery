DEPENDS:append:tcmode-external-sourcery = " linux-libc-headers"
PROVIDES:remove:tcmode-external-sourcery = "\
    linux-libc-headers \
    linux-libc-headers-dev\
"

FILES:${PN}-dev:remove:tcmode-external-sourcery = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

LINUX_LIBC_RDEP_REMOVE:tcmode-external-sourcery = ""
