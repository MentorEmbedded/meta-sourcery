# ---------------------------------------------------------------------------------------------------------------------
# SPDX-License-Identifier: MIT
# ---------------------------------------------------------------------------------------------------------------------

# Include the external toolchain in our Yocto SDKs, not the internal one, by default
BINUTILS:tcmode-external-sourcery ?= ""
GCC:tcmode-external-sourcery ?= ""
GDB:tcmode-external-sourcery ?= ""
RDEPENDS:${PN}:append:tcmode-external-sourcery = " external-toolchain-${TRANSLATED_TARGET_ARCH}"
