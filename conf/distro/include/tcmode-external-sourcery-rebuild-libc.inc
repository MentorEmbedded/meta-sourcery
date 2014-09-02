require conf/distro/include/tcmode-external-sourcery.inc

PREFERRED_PROVIDER_virtual/libc = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libiconv = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libintl = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "glibc-sourcery"
PREFERRED_PROVIDER_glibc = "glibc-sourcery"
CSL_SRC_URI ?= "file://${CSL_SRC_FILE}"
CSL_SRC_FILE ?= "${@bb.fatal('Please set CSL_SRC_FILE to the path to your sourcery src tarball')}"
