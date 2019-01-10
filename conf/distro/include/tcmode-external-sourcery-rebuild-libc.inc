require conf/distro/include/tcmode-external-sourcery.inc

PREFERRED_PROVIDER_virtual/libc = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libiconv = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/libintl = "glibc-sourcery"
PREFERRED_PROVIDER_virtual/crypt = "glibc-sourcery"
PREFERRED_PROVIDER_glibc = "glibc-sourcery"
GLIBC_INTERNAL_USE_BINARY_LOCALE = "compile"
ENABLE_BINARY_LOCALE_GENERATION = "1"
LOCALE_UTF8_IS_DEFAULT_mel = "0"
