# Separated out from the rest of sourcery-tc to make
# the swap between this and eglibc-sourcery-compile.bb cleaner.

# Various default setup (some of which will then be overwritten)
require sourcery-tc-shared.inc

# The basic eglibc packaging.
require eglibc-package-adjusted.inc

# Extract the multilib stuff before trying to do anything else fancy.
do_configure[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"
do_install[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"

# We need the prebuilts in place before doing any of this.
DEPENDS += "sourcery-tc-prebuilt"
# Some stuff (like self-hosting) assumes that installing eglibc
# gets you the headers, too.
RDEPENDS_${PN}-dev += "linux-libc-headers-dev"

# The following is needed for bitbake to find files shared with
# the rebuilding case.
FILESEXTRAPATHS_prepend := "${THISDIR}/eglibc-sourcery:"
SRC_URI = "file://SUPPORTED"

PROVIDES += "\
        ${@base_conditional('PREFERRED_PROVIDER_virtual/libc', BPN, 'virtual/libc virtual/libiconv virtual/libintl virtual/${TARGET_PREFIX}libc-for-gcc ${TCLIBC}', '', d)} \
"

# Define a local PN  (this ensures any virtclass variants on PN are preserved!)
LPN := "${@d.getVar('PN', True).replace("eglibc-sourcery-binary", "eglibc")}"

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

PKGV = "${CSL_VER_LIBC}"

CSL_TARGET_DEV := "${@['/${CSL_TARGET_CORE}','']['${CSL_TARGET_CORE}' == '']}"
FILES_${PN}-dev += "${CSL_TARGET_DEV}"
FILES_${PN}-dbg += "/usr/src ${EXTERNAL_SOURCERY_DEBUGSRC} ${prefix}/libexec/getconf/.debug"

# When extracting from cpio, we need to not have leading slashes. When
# putting things in FILES_${PN}, we need them.
EXTRA_FILES_LIST = "${@" ".join(["/" + x for x in (d.getVar("EXTERNAL_SOURCERY_EXTRA_SYSROOT_FILES", True) or "").split()])}"
FILES_${PN} += "${prefix}/libexec ${EXTRA_FILES_LIST}"

UNUSED_FILES = "Xusr/bin/gdb* Xusr/bin/.debug/gdb* X${base_libdir}/libgcc* X${libdir}/bin/sysroot-gdbserver X${libdir}/libstdc++* X${libdir}/libgomp*"
UNUSED_DIRS = "X${libdir}/bin"

do_install() {
        tccp ${TOOLCHAIN_SYSROOT_COPY}/${base_libdir}/. ${D}/${base_libdir}
        tccp ${TOOLCHAIN_SYSROOT_COPY}/usr/. ${D}/usr
	if [ -n "${EXTRA_FILES_LIST}" ]; then
		for file in ${EXTRA_FILES_LIST}; do
			subdir="$(dirname $file)"
			mkdir -p ${D}${subdir}
			tccp -R ${TOOLCHAIN_SYSROOT_COPY}$file ${D}${subdir}
		done
	fi

	# Kernel headers, if there are any, would have been installed
	# by either the kernel or the base prebuilts package. So we
	# remove them here.

	# Prefix with X so that /usr/include/asm* isn't globbed; then
	# $subdir will end up as "asm*", which should be globbed when
	# it gets to tccp, but then it's got a path in front of it.
	# Note that the tabs after the line continuations count...
	kernel_headers=$(echo " ${LIKELY_KERNEL_HEADERS}" | sed -e 's/[\x09]/ /g' -e 's/  */ X/g')

	for subdir in $kernel_headers; do
		if [ "$subdir" = "X" ]; then
			continue
		fi
		# Handle asm* by globbing in the right directory.
		subdirs="$(cd ${D}${includedir}; echo ${subdir#X${includedir}/})"
		for subsub in $subdirs; do
			rm -rf ${D}${includedir}/$subsub || true
		done
	done

	if [ -d ${D}/usr/src/debug ] && [ x${INHIBIT_PACKAGE_DEBUG_SPLIT} = x1 ] && [ -n "${EXTERNAL_SOURCERY_DEBUGSRC}" ] && [ x${EXTERNAL_SOURCERY_DEBUGSRC} != x/usr/src/debug ]; then
		dir=${EXTERNAL_SOURCERY_DEBUGSRC}
		mkdir -p ${D}${dir%/*}
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
	install -d ${D}/${sbindir}/
	mv ${D}${bindir}/ldconfig  ${D}${base_sbindir}/.
	mv ${D}${bindir}/nscd ${D}${sbindir}/.
	mv ${D}${bindir}/sln ${D}${base_sbindir}/.
	mv ${D}${bindir}/iconvconfig ${D}${sbindir}/.
	mv ${D}${bindir}/zdump ${D}${sbindir}/.
	mv ${D}${bindir}/zic ${D}${sbindir}/.
	touch ${D}/${sysconfdir}/ld.so.conf

	# Remove the unwanted files
	rm -f ${D}${libdir}/libasan*.so.* ${D}${libdir}/libssp*.so.* \
		${D}${libdir}/libatomic*.so.*
	rm -f ${D}${libdir}/libasan*.so ${D}${libdir}/libssp*.so \
		${D}${libdir}/libatomic*.so ${D}${libdir}/libquadmath.so
	rm -f ${D}${libdir}/libasan.a ${D}${libdir}/libssp.a ${D}${libdir}/libssp_nonshared.a \
		${D}${libdir}/libatomic.a ${D}${libdir}/libquadmath.a
	rm -f ${D}${mandir}/man*/gdbserver.*
	rm -f ${D}${libexecdir}/getconf
	rmdir --ignore-fail-on-non-empty ${D}${mandir}/man1
	rmdir --ignore-fail-on-non-empty ${D}${mandir}
	rm -rf  ${D}/usr/src/debug/expat \
		${D}/usr/src/debug/gcc \
		${D}/usr/src/debug/gdb \
		${D}/usr/src/debug/generated/gcc \
		${D}/usr/src/debug/generated/gdb \
		${D}/usr/src/debug/generated/gdb-target \
		
}

do_install_locale_append () {
	if [ -e "${D}${localedir}" ]; then
		rm -r ${D}${localedir}
	fi
}

# Nuke ldconfig if we're not using it.
include conditional-ldconfig.inc
