require recipes-external/gcc/gcc-external.inc
inherit external-toolchain-cross-canadian

PN .= "-${TARGET_ARCH}"
DEPENDS += "virtual/${TARGET_PREFIX}binutils"
PROVIDES += "\
    gcc-cross-canadian-${TARGET_ARCH} \
"

EXTERNAL_CROSS_BINARIES = "${@'${gcc_binaries}'.replace('${TARGET_PREFIX}', '')}"
