FILESEXTRAPATHS_prepend := "${THISDIR}/boot-format:"
SRC_URI += "file://flags.patch"
EXTRA_OEMAKE += "'CFLAGS=${CFLAGS}' 'LDFLAGS=${LDFLAGS}'"
