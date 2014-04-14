# Disable build of gdbserver because it is 
# provided by external-sourcery-toolchain
PACKAGES := "${@oe_filter_out('gdbserver' if '${TCMODE}'.startswith('external-sourcery') else '$', '${PACKAGES}', d)}"
DISABLE_GDBSERVER := "${@'--disable-gdbserver' if '${TCMODE}'.startswith('external-sourcery') else ''}"
EXTRA_OECONF += "${DISABLE_GDBSERVER}"
