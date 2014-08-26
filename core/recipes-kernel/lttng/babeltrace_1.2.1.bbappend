FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append_arm = " file://do-not-optimize-parser.patch"
SRCREV = "7efc443724d404bc06df5d484be36f7b7d2bcf60"
