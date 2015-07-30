SUMMARY = "The GNU Compiler Collection"
HOMEPAGE = "http://www.gnu.org/software/gcc/"
SECTION = "devel"
GCC_VERSION := "${@oe.external.run(d, 'gcc', '-dumpversion').rstrip()}"
PV = "${GCC_VERSION}"

inherit external-toolchain

gcc_binaries = "\
    ${TARGET_PREFIX}gcc \
    ${TARGET_PREFIX}gcc-${@'${PV}'.replace('${EXTERNAL_PV_SUFFIX}', '')} \
    ${TARGET_PREFIX}gcc-ar \
    ${TARGET_PREFIX}gcc-nm \
    ${TARGET_PREFIX}gcc-ranlib \
    ${TARGET_PREFIX}gcov \
    ${TARGET_PREFIX}c++ \
    ${TARGET_PREFIX}g++ \
    ${TARGET_PREFIX}cpp \
"
