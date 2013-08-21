DEFAULTTUNE ?= "ppce500v2"

require conf/machine/include/powerpc/arch-powerpc.inc

TUNEVALID[ppce500v2] = "Enable ppce500v2 specific processor optimizations"

# FIXME
# Workaround for ICE of gcc-4.8.x when passing -mfloat-gprs=double
# Bug: http://gcc.gnu.org/bugzilla/show_bug.cgi?id=57717#c1
# Replace double-float with single-float, revert this when above bug is fixed
PPCE500V2_CCARG = "${@'-te500v2' if d.getVar('CSL_IS_PRO', True) == '1' else '-mcpu=8548 -mabi=spe -mspe -mfloat-gprs=single'}"
TUNE_CCARGS_append = " ${@bb.utils.contains("TUNE_FEATURES", "ppce500v2", PPCE500V2_CCARG, "", d)}"

# Note that SPE is implied by the ppce500v2 feature
TARGET_FPU .= "${@bb.utils.contains("TUNE_FEATURES", "ppce500v2", "ppc-efd", "", d)}"

# spe is defined potentially in two places, so we want to be sure it will
# only write spe once to the ABIEXTENSIONS field.
SPEABIEXTENSION = "${@bb.utils.contains("TUNE_FEATURES", "ppce500v2", "spe", "", d)}"
ABIEXTENSION .= "${SPEABIEXTENSION}"

AVAILTUNES += "ppce500v2"
TUNE_FEATURES_tune-ppce500v2 = "m32 ppce500v2"
TUNE_PKGARCH_tune-ppce500v2 = "ppce500v2"
PACKAGE_EXTRA_ARCHS_tune-ppce500v2 = "ppce500v2"
