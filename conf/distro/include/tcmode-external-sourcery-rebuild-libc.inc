require conf/distro/include/tcmode-external-sourcery.inc

PREFERRED_PROVIDER_virtual/libc = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libiconv = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libintl = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-initial = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "glibc-sourcery"
PREFERRED_PROVIDER_glibc = "glibc-sourcery"
SOURCERY_SRC_URI ?= "file://${SOURCERY_SRC_FILE}"
