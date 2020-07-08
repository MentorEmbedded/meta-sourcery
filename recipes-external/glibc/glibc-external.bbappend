DEPENDS_append_tcmode-external-sourcery = " linux-libc-headers"
PROVIDES_remove_tcmode-external-sourcery = "\
    linux-libc-headers \
    linux-libc-headers-dev\
"

FILES_${PN}-dev_remove_tcmode-external-sourcery = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

LINUX_LIBC_RDEP_REMOVE_tcmode-external-sourcery = ""
