DEPENDS:append:tcmode-external-sourcery = " linux-libc-headers"
PROVIDES:remove:tcmode-external-sourcery = "\
    linux-libc-headers \
    linux-libc-headers-dev\
"
LINUX_LIBC_RDEP_REMOVE:tcmode-external-sourcery = ""

FILES:${PN}-dev:remove:tcmode-external-sourcery = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

remove_sys () {
    rm -rf "${D}${includedir}/sys"
}

do_install[postfuncs] += "remove_sys"
