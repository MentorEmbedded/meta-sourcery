# Disable the prebuilt package.
PNBLACKLIST[eglibc-sourcery-compile] = ""
PNBLACKLIST[eglibc-sourcery-binary] = "You are building libc from source, and cannot use the prebuilt. Check TCMODE_EXTRA."

PREFERRED_PROVIDER_virtual/libc = "eglibc-sourcery-compile"
PREFERRED_PROVIDER_virtual/libiconv = "eglibc-sourcery-compile"
PREFERRED_PROVIDER_virtual/libintl = "eglibc-sourcery-compile"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "eglibc-sourcery-compile"
PREFERRED_PROVIDER_eglibc = "eglibc-sourcery-compile"
CSL_SRC_URI ?= "file://${CSL_SRC_FILE}"
CSL_SRC_FILE ?= "${@bb.fatal('Please set CSL_SRC_FILE to the path to your sourcery src tarball')}"
