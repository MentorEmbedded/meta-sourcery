DEPENDS += "linux-libc-headers"

FILES_${PN}-dev_remove = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"
