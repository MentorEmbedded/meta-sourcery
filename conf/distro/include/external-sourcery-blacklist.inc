# When the meta-sourcery layer is included, only the binary toolchain is
# supported.  Blacklist the standard toolchain components to avoid problems.
META_SOURCERY_BLACKLIST_MESSAGE = "The prebuilt toolchain precludes building certain toolchain packages."

# binutils needs to be allowed for oprofile
# PNBLACKLIST[binutils] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[linux-libc-headers] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[eglibc] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[eglibc-initial] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
# PNBLACKLIST[libiconv] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gdb] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
# PNBLACKLIST[gcc] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-cross-initial] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[external-csl-toolchain] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[packagegroup-self-hosted] = "${META_SOURCERY_BLACKLIST_MESSAGE}"

PNBLACKLIST[binutils-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-runtime] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gdb-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[mklibs-native] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[prelink-native] = "${META_SOURCERY_BLACKLIST_MESSAGE}"

# Additional things that need to be blacklisted so that bitbake world
# doesn't fail because of the above.
# PNBLACKLIST[cpp] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
# PNBLACKLIST[gccmakedep] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[task-self-hosted] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[task-core-sdk] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[task-core-sdk-gmae] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
