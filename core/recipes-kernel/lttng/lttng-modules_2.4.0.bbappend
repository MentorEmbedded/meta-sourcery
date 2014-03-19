FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_arm = " file://whitelist-sourcery-codebench-2013.11-32.patch"
COMPATIBLE_HOST = '(x86_64|i.86|powerpc|arm|aarch64|mips).*-linux'
