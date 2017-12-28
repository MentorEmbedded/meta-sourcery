DEPENDS += "linux-libc-headers"

FILES_${PN}-dev_remove = "${@' '.join('${includedir}/%s' % d for d in '${linux_include_subdirs}'.split())}"

python () {
    # bits/syscall.h is in linux-libc-headers-external
    install = d.getVar('do_install_glibc', False)
    install = install.replace('oe_multilib_header bits/syscall.h bits/long-double.h\n', '')
    d.setVar('do_install_glibc', install)
    
}
