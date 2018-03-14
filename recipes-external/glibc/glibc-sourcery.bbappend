# Avoid bash dependency via bbappend because the glibc-sourcery recipe from
# the Updates-2 layer is being used and that layer is locked now.
do_install_append_tcmode-external-sourcery () {
    # Avoid bash dependency
    sed -e '1s#bash#sh#; s#$"#"#g' -i "${D}${bindir}/ldd"
    sed -e '1s#bash#sh#' -i "${D}${bindir}/tzselect"
    sed -e '1s#bash#sh#' -i "${D}/etc/init.d/nscd"
}
