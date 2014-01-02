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
PNBLACKLIST[gcc] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[libgcc] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-cross-initial] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[external-csl-toolchain] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[prelink] = "${META_SOURCERY_BLACKLIST_MESSAGE}"

PNBLACKLIST[binutils-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gcc-runtime] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[gdb-cross] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[mklibs-native] = "${META_SOURCERY_BLACKLIST_MESSAGE}"
PNBLACKLIST[prelink-native] = "${META_SOURCERY_BLACKLIST_MESSAGE}"

# Requires gcc...
GCCMAKEDEPBLMSG = "Using the prebuilt toolchain precludes building gcc which is required by this package."
GCCMAKEDEPBLMSG_i686 = ""
GCCMAKEDEPBLMSG_i586 = ""
PNBLACKLIST[gccmakedep] = "${GCCMAKEDEPBLMSG}"

PGTCDEPBLMSG = "Using the prebuilt toolchain precludes building this packagegroup."
PGTCDEPBLMSG_x86-64 = ""
PGTCDEPBLMSG_i686 = ""
PGTCDEPBLMSG_i586 = ""
PNBLACKLIST[packagegroup-core-buildessential] = "${PGTCDEPBLMSG}"
PNBLACKLIST[packagegroup-core-sdk] = "${PGTCDEPBLMSG}"
PNBLACKLIST[packagegroup-self-hosted] = "${PGTCDEPBLMSG}"
PNBLACKLIST[packagegroup-core-sdk-gmae] = "${PGTCDEPBLMSG}"
