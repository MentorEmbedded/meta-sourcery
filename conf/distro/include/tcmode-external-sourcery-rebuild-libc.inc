require conf/distro/include/tcmode-external-sourcery.inc

PREFERRED_PROVIDER_virtual/libc = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/libiconv = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/libintl = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "eglibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-initial ?= "eglibc-sourcery"
PREFERRED_PROVIDER_eglibc = "eglibc-sourcery"
CSL_SRC_URI ?= "file://${CSL_SRC_FILE}"
CSL_SRC_FILE ?= "${@bb.fatal('Please set CSL_SRC_FILE to the path to your sourcery src tarball')}"
