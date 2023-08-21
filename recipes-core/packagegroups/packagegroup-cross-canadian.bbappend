# ---------------------------------------------------------------------------------------------------------------------
# SPDX-License-Identifier: MIT
# ---------------------------------------------------------------------------------------------------------------------

# Include the external toolchain in our Yocto SDKs, not the internal one, by default
BINUTILS:tcmode-external-sourcery ?= ""
GCC:tcmode-external-sourcery ?= ""
GDB:tcmode-external-sourcery ?= ""

# Use indirection to stop this being expanded prematurely
TOOLCHAIN_PACKAGE ?= "external-toolchain-${TRANSLATED_TARGET_ARCH}"
RDEPENDS:${PN}:append:tcmode-external-sourcery = " ${@all_multilib_tune_values(d, 'TOOLCHAIN_PACKAGE')}"
