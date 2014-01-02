# Disable the prebuilt package.
PNBLACKLIST[binutils] = ""
PNBLACKLIST[gcc] = ""
PNBLACKLIST[prelink] = ""
PNBLACKLIST[gccmakedep] = ""
PNBLACKLIST[packagegroup-core-buildessential] = ""
PNBLACKLIST[packagegroup-core-sdk] = ""
PNBLACKLIST[packagegroup-self-hosted] = ""
PNBLACKLIST[packagegroup-core-sdk-gmae] = ""
PNBLACKLIST[sourcery-tc] = "You are building the toolchain from source, and cannot use the prebuilt. Check TCMODE_EXTRA."

# Restore the preferred provider
PREFERRED_PROVIDER_binutils = "binutils"
PREFERRED_PROVIDER_gcc      = "gcc"
PREFERRED_PROVIDER_prelink  = "prelink"

VIRTUAL-RUNTIME_toolchain        = "binutils cpp gcc g++"
VIRTUAL-RUNTIME_toolchain_x86-64 = "binutils cpp gcc g++"

