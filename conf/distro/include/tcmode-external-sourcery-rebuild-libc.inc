require conf/distro/include/tcmode-external-sourcery.inc

# Disable the prebuilt package.
PNBLACKLIST[eglibc-sourcery] = ""
PNBLACKLIST[eglibc-sourcery-prebuilt] = "You are building libc from source, and cannot use the prebuilt. Check TCMODE."

PREFERRED_PROVIDER_virtual/libc = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/libiconv = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/libintl = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "eglibc-sourcery"
PREFERRED_PROVIDER_eglibc = "eglibc-sourcery"
CSL_SRC_URI ?= "file://${CSL_SRC_FILE}"
CSL_SRC_FILE ?= "${@bb.fatal('Please set CSL_SRC_FILE to the path to your sourcery src tarball')}"
