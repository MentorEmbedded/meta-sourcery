# Disable build of gdbserver because it is 
# provided by external-sourcery-toolchain
PACKAGES := "${@oe_filter_out('gdbserver', '${PACKAGES}', d)}"
EXTRA_OECONF += "--disable-gdbserver"
