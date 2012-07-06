DEFAULTTUNE ?= "ppce500"

require conf/machine/include/powerpc/arch-powerpc.inc

TUNEVALID[ppce500] = "Enable ppce500 specific processor optimizations"
TUNE_CCARGS += "${@bb.utils.contains("TUNE_FEATURES", "ppce500", "-te500", "", d)}"

# Note that SPE is implied by the ppce500 feature
TARGET_FPU .= "${@bb.utils.contains("TUNE_FEATURES", "ppce500", "ppc-efs", "", d)}"

# spe is defined potentially in two places, so we want to be sure it will
# only write spe once to the ABIEXTENSIONS field.
SPEABIEXTENSION = "${@bb.utils.contains("TUNE_FEATURES", "ppce500", "spe", "", d)}"
ABIEXTENSION .= "${SPEABIEXTENSION}"

AVAILTUNES += "ppce500"
TUNE_FEATURES_tune-ppce500 = "m32 ppce500"
TUNE_PKGARCH_tune-ppce500 = "ppce500"
PACKAGE_EXTRA_ARCHS_tune-ppce500 = "ppce500"
