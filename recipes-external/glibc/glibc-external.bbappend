DEPENDS_append_tcmode-external-sourcery = " linux-libc-headers"

FILES_${PN}-dev_remove_tcmode-external-sourcery = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"
