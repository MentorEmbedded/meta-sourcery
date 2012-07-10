DEFAULTTUNE ?= "ppce500"

require conf/machine/include/powerpc/arch-powerpc.inc

TUNE_PKGARCH = "${TUNE_PKGARCH_tune-${DEFAULTTUNE}}"
TUNE_PKGARCH_tune-powerpc = "powerpc"
TUNE_PKGARCH_tune-powerpc-nf = "powerpc-nf"

TUNEVALID[ppce500] = "Enable ppce500 specific processor optimizations"
TUNE_CCARGS += "${@bb.utils.contains("TUNE_FEATURES", "ppce500", "-te500", "", d)}"

# Note that SPE is implied by the ppce500 feature
TARGET_FPU .= "${@bb.utils.contains("TUNE_FEATURES", "ppce500", "ppc-efs", "", d)}"

AVAILTUNES += "ppce500"
TUNE_FEATURES_tune-ppce500 = "m32 ppce500"
TUNE_PKGARCH_tune-ppce500 = "ppce500"
PACKAGE_EXTRA_ARCHS_tune-ppce500 = "ppce500"
