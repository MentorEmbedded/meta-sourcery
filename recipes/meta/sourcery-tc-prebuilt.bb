#
# Copyright (C) 2012, 2013 Wind River Systems, Inc.
#
# Target binaries which are used whether or not we rebuild glibc.

include sourcery-tc-shared.inc

# Extract the multilib stuff before trying to do anything else fancy.
do_configure[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"
do_install[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"

# Make sure the binary links get created too, even if this is
# pulled in from sstate.
SSTATEPOSTINSTFUNCS += " external_toolchain_binary_links"

BASEDEPENDS = ""

PKGV = "${CSL_VER_GCC}"
PR = "r0"

# List libgomp before libgcc so it won't inadvertantly pick up
# some of libgomp's files.
PROVIDES += " \
	eglibc-source-dbg \
        ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers', '', d)} \
        ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'virtual/linux-libc-headers', '', d)} \
        libgomp \
        libgomp-staticdev \
        libgomp-dev \
        libgomp-dbg \
        libgcc \
        libgcc-dev \
        libstdc++ \
        libstdc++-dev \
        libstdc++-staticdev \
	gdb \
	gdbserver \
	virtual/${TARGET_PREFIX}libc-initial \
"

PACKAGES = "eglibc-source-dbg gdb gdb-dbg gdbserver gdbserver-dbg \
             libstdc++ libstdc++-dev libstdc++-dbg libstdc++-staticdev \
             ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers', '', d)} \
             ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers-dev', '', d)} \
             libgomp libgomp-staticdev libgomp-dev libgomp-dbg \
             libgcc libgcc-dev \
             "

PKGV_eglibc-source-dbg = "${CSL_VER_LIBC}"
PKGV_gdb = "${CSL_VER_GDB}"
PKGV_gdb-dbg = "${CSL_VER_GDB}"
PKGV_gdbserver = "${CSL_VER_GDB}"
PKGV_gdbserver-dbg = "${CSL_VER_GDB}"
PKGV_linux-libc-headers = "${CSL_VER_KERNEL}"
PKGV_linux-libc-headers-dev = "${CSL_VER_KERNEL}"
PKGV_libgcc = "${CSL_VER_GCC}"
PKGV_libgcc-dev = "${CSL_VER_GCC}"
PKGV_libgomp = "${CSL_VER_GCC}"
PKGV_libgomp-dev = "${CSL_VER_GCC}"
PKGV_libgomp-staticdev = "${CSL_VER_GCC}"
PKGV_libgomp-dbg = "${CSL_VER_GCC}"
PKGV_libstdc++ = "${CSL_VER_GCC}"
PKGV_libstdc++-dev = "${CSL_VER_GCC}"
PKGV_libstdc++-dbg = "${CSL_VER_GCC}"
PKGV_libstdc++-staticdev = "${CSL_VER_GCC}"

FILES_eglibc-source-dbg = "/usr/src/debug ${EXTERNAL_SOURCERY_DEBUGSRC}"
FILES_gdb = "${bindir}/gdb"
FILES_gdb-dbg = "${bindir}/.debug/gdb"
RDEPENDS_gdb_append = " glibc-thread-db "
FILES_gdbserver = "${bindir}/gdbserver ${libdir}/bin/sysroot-gdbserver"
FILES_gdbserver-dbg = "${bindir}/.debug/gdbserver ${libdir}/bin/.debug/sysroot-gdbserver"
RDEPENDS_gdbserver_append = " glibc-thread-db "
FILES_libgomp = "${libdir}/libgomp.so.*"
FILES_libgomp-dev = "${libdir}/libgomp.so"
FILES_libgomp-staticdev = "${libdir}/libgomp.a"
FILES_libgomp-dbg = "${libdir}/.debug/libgomp*"
FILES_libgcc = "${base_libdir}/libgcc_s.so.1 /usr/lib"
# /usr/lib has to exist for the compiler to work.
# CSL_TARGET_CORE is the (possibly empty, if none's needed) directory
# containing the symlink back to the sysroot for a multilib.
FILES_libgcc-dev = "${base_libdir}/libgcc_s.so ${@'/' + CSL_TARGET_CORE if CSL_TARGET_CORE else ''}"
FILES_linux-libc-headers = ""
# This name exists to be a name which won't be renamed for multilib
# builds.
LIKELY_KERNEL_HEADERS = "${includedir}/asm* \
	${includedir}/linux \
	${includedir}/mtd \
	${includedir}/rdma \
	${includedir}/scsi \
	${includedir}/sound \
	${includedir}/video \
"
FILES_linux-libc-headers-dev = "${LIKELY_KERNEL_HEADERS}"

FILES_libstdc++ = "${libdir}/libstdc++.so.*"
FILES_libstdc++-dbg = "${libdir}/.debug/libstdc++* ${datadir}/gdb/*"
FILES_libstdc++-dev = "${includedir}/c++/${PV} \
	${libdir}/libstdc++.so \
	${libdir}/libstdc++.la \
	${libdir}/libsupc++.la"
FILES_libstdc++-staticdev = "${libdir}/libstdc++.a ${libdir}/libsupc++.a"

# usage:
# cpioextract <cpio_archive> <destination> <paths>
cpioextract() {
        cpio_file=$1
        shift 1
        cpio_dest=$1
        shift 1
        echo "Unpacking from cpio..."
        if [ ! -f "$cpio_file" ] && [ -f "$cpio_file.gz" ]; then
                ( cd $cpio_dest; gzip -dc $cpio_file.gz | cpio -idm "$@" )
        else
                ( cd $cpio_dest; cpio -idm "$@" < $cpio_file )
        fi
}

USR_SUBDIRS = "bin ${baselib} libexec sbin share lib/locale"

toolchain_binary_install() {
        libc=$1
        libpath=$2
        tc_source="$libc${libpath:+/}$libpath"
        echo "tc_source: $tc_source ($libpath in $libc)"

        mkdir -p "${TOOLCHAIN_SYSROOT_COPY}"

        if [ -d $tc_source ] && [ -e $tc_source/usr/${baselib}/libc.so ]; then
                sysroot=$tc_source
                echo "Extracting prebuilt binaries from $tc_source."
                tccp $sysroot/${baselib}/. ${TOOLCHAIN_SYSROOT_COPY}/${base_libdir}
                tccp $sysroot/etc/. ${TOOLCHAIN_SYSROOT_COPY}${sysconfdir}
                tccp $sysroot/sbin/. ${TOOLCHAIN_SYSROOT_COPY}${base_sbindir}
                mkdir -p ${TOOLCHAIN_SYSROOT_COPY}/usr
                for subdir in ${USR_SUBDIRS}; do
                        mkdir -p ${TOOLCHAIN_SYSROOT_COPY}/usr/$subdir
                        tccp $sysroot/usr/$subdir/. ${TOOLCHAIN_SYSROOT_COPY}/usr/$subdir
                done
                mkdir -p ${TOOLCHAIN_SYSROOT_COPY}${includedir}
                tccp $libc/usr/include/. ${TOOLCHAIN_SYSROOT_COPY}${includedir}
        elif [ -f $tc_source.cpio ] || [ -f $tc_source.cpio.gz ]; then
                tc_source="$tc_source.cpio"
                subdirs=""
                for subdir in ${USR_SUBDIRS}; do
                        subdirs="$subdirs usr/$subdir/* "
                done
                echo "Extracting prebuilt binaries from $tc_source."
                cpioextract "$tc_source" ${TOOLCHAIN_SYSROOT_COPY} ${baselib}/\* etc/\* sbin/\* $subdirs
                echo "Extracting generic headers from $libc.cpio"
                cpioextract $libc.cpio ${TOOLCHAIN_SYSROOT_COPY} usr/include/\*
        else
                echo >&2 "No $tc_source (or .cpio)."
                $warn_or_fail
        fi

        if [ ! -e ${TOOLCHAIN_SYSROOT_COPY}/usr/${baselib}/libc.so ]; then
                echo >&2 "No libc.so found in $tc_source."
                $warn_or_fail
        fi
}

# Shareable setup code.
do_setup_shared() {
	# Make sure the toolchain binary symlinks are handy.
	external_toolchain_binary_links

        # This section copies the tuning-specific sysroot and other
        # files into the shared directory ${TOOLCHAIN_SYSROOT_COPY},
        # to make them available for other packages.

	# Use optimized files if available
	base_sysroot="${EXTERNAL_TOOLCHAIN}/${CSL_TARGET_SYS}/libc"
        libc_path="${CSL_TARGET_CORE}"

	# Source corresponding to debug info. This is then fixed
	# up by debugedit.
	srcroot="${EXTERNAL_TOOLCHAIN}/${CSL_TARGET_SYS}/src"
	srctarget="${TOOLCHAIN_SYSROOT_COPY}/usr/src/debug"

	mkdir -p "$srctarget"
	tccp "$srcroot"/. "$srctarget"
	chmod -R u+w "$srctarget"

        warn_or_fail=false

        toolchain_binary_install $base_sysroot $libc_path

        warn_or_fail=true

        override_type=""
        case "${SELECTED_OPTIMIZATION}" in
        *fno-omit-frame-pointer*)
                override_type="_profiling"
                ;;
        esac
        if [ -n "$override_type" ]; then
                toolchain_binary_install $base_sysroot$override_type $libc_path
        fi

        # cleanup that only has to happen once after (possibly-both) binaries
        # are installed.

        mkdir -p ${TOOLCHAIN_SYSROOT_COPY}${bindir}
	mv ${TOOLCHAIN_SYSROOT_COPY}${libdir}/bin/* ${TOOLCHAIN_SYSROOT_COPY}${bindir}/.
	if [ -e ${TOOLCHAIN_SYSROOT_COPY}${libdir}/bin/.debug ]; then
		if [ ! -d ${TOOLCHAIN_SYSROOT_COPY}${bindir}/.debug ] ; then
			install -d ${TOOLCHAIN_SYSROOT_COPY}${bindir}/.debug
		fi
		mv ${TOOLCHAIN_SYSROOT_COPY}${libdir}/bin/.debug/* ${TOOLCHAIN_SYSROOT_COPY}${bindir}/.debug/
	fi

	# This duplicates a line in eglibc-package.inc; the reason we
	# do it here is that it's in do_install_append(), which runs
	# after this, and if we don't remove this first then we can't
	# rmdir the then-empty /etc, resulting in a QA warning.
	rm -f ${TOOLCHAIN_SYSROOT_COPY}${sysconfdir}/localtime

	# .keep_me files exist to make git work, but leaving them results
	# in warnings about unpackaged files; also remove the directories
	# if we can.
	for dir in ${sbindir} ${base_sbindir} ${bindir} ${sysconfdir}; do
		rm ${TOOLCHAIN_SYSROOT_COPY}$dir/.keep_me 2>/dev/null || true
		rmdir ${TOOLCHAIN_SYSROOT_COPY}$dir 2>/dev/null || true
	done

	rm -f ${TOOLCHAIN_SYSROOT_COPY}${sysconfdir}/rpc
	rm -rf ${TOOLCHAIN_SYSROOT_COPY}${datadir}/zoneinfo
	# The info/dir file can clash with other packages, and should
	# never be installed.
	rm -f ${TOOLCHAIN_SYSROOT_COPY}/usr/share/info/dir
	ln -sf ../../bin/gdbserver ${TOOLCHAIN_SYSROOT_COPY}${libdir}/bin/sysroot-gdbserver

	# These are unpackaged, squelch warning
	rm -f ${TOOLCHAIN_SYSROOT_COPY}/usr/${baselib}/libquadmath.so.0
	rm -f ${TOOLCHAIN_SYSROOT_COPY}/usr/${baselib}/libquadmath.so.0.0.0
}

addtask do_setup_shared

DO_HEADERS = "${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'true', 'false', d)}"
do_install() {
	install -d ${D}${bindir}
	install -d ${D}${libdir}/bin

	if [ x${INHIBIT_PACKAGE_DEBUG_SPLIT} = x1 ] ; then
		# In the case where we do not split the debug information
		# The sources need to go to the real location they would have
		# been located on the target system, or they will not be found
		if [ -n "${EXTERNAL_SOURCERY_DEBUGSRC}" ]; then
			install -d ${D}${EXTERNAL_SOURCERY_DEBUGSRC}
			tccp ${TOOLCHAIN_SYSROOT_COPY}/usr/src/debug/. ${D}${EXTERNAL_SOURCERY_DEBUGSRC}
		else
			tccp ${TOOLCHAIN_SYSROOT_COPY}/usr/src/debug/. ${D}/usr/src/debug
		fi
	else
		install -d ${D}/usr/src
		tccp ${TOOLCHAIN_SYSROOT_COPY}/usr/src/debug/. ${D}/usr/src/debug
	fi
        tccp ${TOOLCHAIN_SYSROOT_COPY}/${bindir}/gdb ${D}${bindir}/.
        tccp ${TOOLCHAIN_SYSROOT_COPY}/${bindir}/gdbserver ${D}${bindir}/.
        ln -sf ../../bin/gdbserver ${D}${libdir}/bin/sysroot-gdbserver

        # install libgcc, libstdc++, and maybe kernel headers
        # (prebuilt headers other than the kernheaders are over in wrl-glibc-*.)
	install -d ${D}${base_libdir}
	install -d ${D}${libdir}

	if ${DO_HEADERS}; then
		install -d ${D}${includedir}
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
			subdirs="$(cd ${TOOLCHAIN_SYSROOT_COPY}${includedir}; echo ${subdir#X${includedir}/})"
			for subsub in $subdirs; do
				install -d ${D}${includedir}/$subsub
				tccp ${TOOLCHAIN_SYSROOT_COPY}${includedir}/$subsub/. ${D}${includedir}/$subsub
			done
		done
	fi
        tccp ${TOOLCHAIN_SYSROOT_COPY}${base_libdir}/libgcc* ${D}${base_libdir}
        tccp ${TOOLCHAIN_SYSROOT_COPY}${libdir}/libgomp* ${D}${libdir}
        tccp ${TOOLCHAIN_SYSROOT_COPY}${libdir}/libstdc++* ${D}${libdir}
        
        external_toolchain_links ${D}

	install -d ${D}${datadir}/gdb/auto-load
	if [ -f ${D}${libdir}/libstd*.py ]; then
		mv ${D}${libdir}/libstd*.py  ${D}${datadir}/gdb/auto-load
	fi
}
