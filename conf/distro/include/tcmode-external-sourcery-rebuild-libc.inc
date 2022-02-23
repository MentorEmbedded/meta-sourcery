require conf/distro/include/tcmode-external-sourcery.inc

TCMODEOVERRIDES .= ":tcmode-external-sourcery-rebuild-libc"

SKIP_RECIPE[glibc] = ""
PREFERRED_PROVIDER_glibc = "glibc"
PREFERRED_PROVIDER_virtual/libc = "glibc"
PREFERRED_PROVIDER_virtual/libintl = "glibc"
PREFERRED_PROVIDER_virtual/libiconv = "glibc"
