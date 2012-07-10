DEFAULTTUNE ?= "ppce500v2"

require conf/machine/include/powerpc/arch-powerpc.inc

TUNE_PKGARCH = "${TUNE_PKGARCH_tune-${DEFAULTTUNE}}"
TUNE_PKGARCH_tune-powerpc = "powerpc"
TUNE_PKGARCH_tune-powerpc-nf = "powerpc-nf"

TUNEVALID[ppce500v2] = "Enable ppce500v2 specific processor optimizations"
TUNE_CCARGS += "${@bb.utils.contains("TUNE_FEATURES", "ppce500v2", "-te500v2", "", d)}"

# Note that SPE is implied by the ppce500v2 feature
TARGET_FPU .= "${@bb.utils.contains("TUNE_FEATURES", "ppce500v2", "ppc-efd", "", d)}"

AVAILTUNES += "ppce500v2"
TUNE_FEATURES_tune-ppce500v2 = "m32 ppce500v2"
TUNE_PKGARCH_tune-ppce500v2 = "ppce500v2"
PACKAGE_EXTRA_ARCHS_tune-ppce500v2 = "ppce500v2"
