FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_arm = " file://do-not-optimize-parser.patch"
