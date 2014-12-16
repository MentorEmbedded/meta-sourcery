SOURCERY_GXX_IS_PRO = "${@'1' if os.path.exists('${EXTERNAL_TOOLCHAIN}/license') else '0'}"

# Workaround for ICE of gcc-4.8.x when passing -mfloat-gprs=double
# Bug: http://gcc.gnu.org/bugzilla/show_bug.cgi?id=57717#c1
# Replace double-float with single-float, revert this when above bug is fixed
PPCE500V2_CCARG = "${@'-te500v2' if d.getVar('SOURCERY_GXX_IS_PRO', True) == '1' else '-mcpu=8548 -mabi=spe -mspe -mfloat-gprs=single'}"
TUNE_CCARGS_append = "${@bb.utils.contains('TUNE_FEATURES', 'ppce500v2', ' ' + d.getVar('PPCE500V2_CCARG', True), '', d)}"

PPCE500MC_CCARG = "${@'-te500mc' if d.getVar('SOURCERY_GXX_IS_PRO', True) == '1' else '-mcpu=e500mc'}"
TUNE_CCARGS_append = "${@bb.utils.contains('TUNE_FEATURES', 'ppce500mc', ' ' + d.getVar('PPCE500MC_CCARG', True), '', d)}"
