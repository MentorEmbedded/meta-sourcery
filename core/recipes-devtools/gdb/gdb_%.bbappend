PROVIDES += "${@'gdbserver' if '${PREFERRED_PROVIDER_gdbserver}' == '${PN}' else ''}"

# Disable build of gdbserver if is provided by external-sourcery-toolchain
PACKAGES := "${@oe_filter_out('gdbserver' if '${PREFERRED_PROVIDER_gdbserver}' != '${PN}' else '$', '${PACKAGES}', d)}"
DISABLE_GDBSERVER := "${@'--disable-gdbserver' if '${PREFERRED_PROVIDER_gdbserver}' != '${PN}' else ''}"
EXTRA_OECONF += "${DISABLE_GDBSERVER}"
