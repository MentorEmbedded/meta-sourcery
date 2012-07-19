DEFAULTTUNE ?= "ppce500mc"

require conf/machine/include/powerpc/arch-powerpc.inc

TUNEVALID[ppce500mc] = "Enable ppce500mc specific processor optimizations"

PPCE500MC_CCARG = "${@'-te500mc' if CSL_IS_PRO == '1' else '-mcpu=e500mc'}"
TUNE_CCARGS += "${@bb.utils.contains("TUNE_FEATURES", "ppce500mc", PPCE500MC_CCARG, "", d)}"

AVAILTUNES += "ppce500mc"
TUNE_FEATURES_tune-ppce500mc = "m32 fpu-hard ppce500mc"
TUNE_PKGARCH_tune-ppce500mc = "ppce500mc"
PACKAGE_EXTRA_ARCHS_tune-ppce500mc = "${PACKAGE_EXTRA_ARCHS_tune-powerpc} ppce500mc"

# glibc configure options to get e500mc specific library (for sqrt)
GLIBC_EXTRA_OECONF += "${@bb.utils.contains("TUNE_FEATURES", "ppce500mc", "-with-cpu=e500mc", "", d)}"
