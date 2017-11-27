FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://external-codebench-toolchains.sh"

do_install_append () {
    install -m 0644 -o root -g root "${WORKDIR}/external-codebench-toolchains.sh" "${D}/environment-setup.d/"
}
