FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_arm = " file://compile-consumers-with-o1.patch"
