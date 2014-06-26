FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_fslmachine = " file://ldflags.patch"
