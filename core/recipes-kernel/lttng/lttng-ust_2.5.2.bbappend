FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"
SRC_URI_append = " file://0001-Fix-Make-deadlocks-with-baddr-statedump-unlikely.patch"
