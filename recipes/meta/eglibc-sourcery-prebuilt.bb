# Separated out from the rest of external-sourcery-toolchain to make
# the swap between this and eglibc-sourcery.bb cleaner.

# Various default setup (some of which will then be overwritten)
require external-sourcery-shared.inc

# The basic eglibc packaging.
require eglibc-package-adjusted.inc

# Extract the multilib stuff before trying to do anything else fancy.
do_configure[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"
do_install[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"

DEPENDS += "external-sourcery-prebuilt"

do_configure[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"
do_install[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"

# The following is needed for bitbake to find files shared with
# the rebuilding case.
FILESEXTRAPATHS_prepend := "${THISDIR}/eglibc-sourcery:"
SRC_URI = "file://SUPPORTED"

PROVIDES += "\
        ${@base_conditional('PREFERRED_PROVIDER_virtual/libc', PN, 'virtual/libc virtual/libiconv virtual/libintl virtual/${TARGET_PREFIX}libc-for-gcc ${TCLIBC}', '', d)} \
"

# Define a local PN  (this ensures any virtclass variants on PN are preserved!)
LPN := "${@d.getVar('PN', True).replace("eglibc-sourcery-prebuilt", "eglibc")}"

#RDEPENDS_${PN}-dev += "linux-libc-headers-dev"

# Adapt any defaults to the LPN version
RPROVIDES_${PN}-pic += "${LPN}-pic"
RPROVIDES_${PN}-mtrace += "${LPN}-mtrace"
RPROVIDES_${PN} += "${LPN}"
RPROVIDES_${PN}-doc += "${LPN}-doc"
RPROVIDES_${PN}-staticdev += "${LPN}-staticdev"
RPROVIDES_${PN}-pcprofile += "${LPN}-pcprofile"
RPROVIDES_${PN}-utils += "${LPN}-utils"
RPROVIDES_${PN}-dev += "${LPN}-dev crt1-dot-o-${SITEINFO_BITS}"
RPROVIDES_${PN}-dbg += "${LPN}-dbg eglibc-dbg"

# This test should be fixed to ignore .a files in .debug dirs
INSANE_SKIP_${PN}-dbg = "staticdev"

PKG_${PN} = "eglibc"
PKG_${PN}-dev = "eglibc-dev"
PKG_${PN}-staticdev = "eglibc-staticdev"
PKG_${PN}-doc = "eglibc-doc"
PKG_${PN}-dbg = "eglibc-dbg"
PKG_${PN}-pic = "eglibc-pic"
PKG_${PN}-utils = "eglibc-utils"
PKG_${PN}-gconv = "eglibc-gconv"
PKG_${PN}-extra-nss = "eglibc-extra-nss"
PKG_${PN}-pcprofile = "eglibc-pcprofile"

PKGV = "${CSL_VER_LIBC}"

CSL_TARGET_DEV := "${@['/${CSL_TARGET_CORE}','']['${CSL_TARGET_CORE}' == '']}"
FILES_${PN}-dev += "${CSL_TARGET_DEV}"
FILES_${PN}-dbg += "/usr/src ${EXTERNAL_SOURCERY_DEBUGSRC}"

UNUSED_FILES = "Xusr/bin/gdb* Xusr/bin/.debug/gdb* X${base_libdir}/libgcc* X${libdir}/bin/sysroot-gdbserver X${libdir}/libstdc++* X${libdir}/libgomp*"
UNUSED_DIRS = "X${libdir}/bin"

do_install() {
        tccp ${TOOLCHAIN_SYSROOT_COPY}/${base_libdir}/. ${D}/${base_libdir}
        tccp ${TOOLCHAIN_SYSROOT_COPY}/usr/. ${D}/usr
	if [ -d ${D}/usr/src/debug -a x${INHIBIT_PACKAGE_DEBUG_SPLIT} = x1 -a -n "${EXTERNAL_SOURCERY_DEBUGSRC}" ] ; then
		mkdir -p ${D}${EXTERNAL_SOURCERY_DEBUGSRC}
		mv ${D}/usr/src/debug ${D}${EXTERNAL_SOURCERY_DEBUGSRC}
	fi
        for file in ${UNUSED_FILES}; do
                rm -f ${D}/${file#X} || true
        done
        for dir in ${UNUSED_DIRS}; do
                rmdir ${D}/${dir#X} || true
        done

	install -d ${D}/${sysconfdir}/
	install -d ${D}/${base_sbindir}/
	mv ${D}${bindir}/ldconfig  ${D}${base_sbindir}
	touch ${D}/${sysconfdir}/ld.so.conf
}

# Nuke ldconfig if we're not using it.
include conditional-ldconfig.inc
