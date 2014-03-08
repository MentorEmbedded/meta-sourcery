FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_arm = "\
    file://blacklist-arm-gcc-4.8.0-4.8.1-4.8.2.patch \
    file://whitelist-sourcery-codebench-2013.11-32.patch \
"
EXTRA_OECONF_append_arm = " --disable-compiler-tls"
