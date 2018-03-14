FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_tcmode-external-sourcery = " file://external-codebench-toolchains.sh"

do_install_append_tcmode-external-sourcery () {
    install -m 0644 -o root -g root "${WORKDIR}/external-codebench-toolchains.sh" "${D}/environment-setup.d/"
}
