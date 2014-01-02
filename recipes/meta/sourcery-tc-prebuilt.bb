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

# List libgomp before libgcc so it won't inadvertantly pick up
# some of libgomp's files.
PROVIDES += " \
	${PN}-dbg \
        ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers', '', d)} \
        ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'virtual/linux-libc-headers', '', d)} \
        libgomp \
        libgomp-staticdev \
        libgomp-dev \
        libgomp-dbg \
        libgcc \
        libgcc-dev \
        libgcov-dev \
        libstdc++ \
        libstdc++-dev \
        libstdc++-staticdev \
	libssp \
	libssp-dev \
	libssp-staticdev \
	gdb \
	gdbserver \
	virtual/${TARGET_PREFIX}libc-initial \
"

PACKAGES = "gdb gdb-dbg gdbserver gdbserver-dbg \
             libstdc++ libstdc++-dev libstdc++-dbg libstdc++-staticdev \
             ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers', '', d)} \
             ${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', BPN, 'linux-libc-headers-dev', '', d)} \
             libgomp libgomp-staticdev libgomp-dev libgomp-dbg \
             libgcc libgcov-dev libssp libssp-dev libssp-staticdev libgcc-dbg libgcc-dev \
             ${PN}-dbg \
             "

PKGV_${PN}-dbg = "${CSL_VER_LIBC}"
PKGV_gdb = "${CSL_VER_GDB}"
PKGV_gdb-dbg = "${CSL_VER_GDB}"
PKGV_gdbserver = "${CSL_VER_GDB}"
PKGV_gdbserver-dbg = "${CSL_VER_GDB}"
PKGV_linux-libc-headers = "${CSL_VER_KERNEL}"
PKGV_linux-libc-headers-dev = "${CSL_VER_KERNEL}"
PKG_libgcc = "libgcc1"
PKGV_libgcc = "${CSL_VER_GCC}"
PKG_libgcc-dev = "libgcc-s-dev"
PKGV_libgcc-dev = "${CSL_VER_GCC}"
PKGV_libgcov-dev = "${CSL_VER_GCC}"
PKG_libssp = "libssp0"
PKGV_libssp = "${CSL_VER_GCC}"
PKGV_libssp-dev = "${CSL_VER_GCC}"
PKGV_libssp-staticdev = "${CSL_VER_GCC}"
PKG_libgcc-dbg = "libgcc-s-dbg"
PKGV_libgcc-dbg = "${CSL_VER_GCC}"
PKGV_libgomp = "${CSL_VER_GCC}"
PKGV_libgomp-dev = "${CSL_VER_GCC}"
PKGV_libgomp-staticdev = "${CSL_VER_GCC}"
PKGV_libgomp-dbg = "${CSL_VER_GCC}"
PKGV_libstdc++ = "${CSL_VER_GCC}"
PKGV_libstdc++-dev = "${CSL_VER_GCC}"
PKGV_libstdc++-dbg = "${CSL_VER_GCC}"
PKGV_libstdc++-staticdev = "${CSL_VER_GCC}"

FILES_${PN}-dbg = "/usr/src/debug ${EXTERNAL_SOURCERY_DEBUGSRC}"
FILES_gdb = "${bindir}/gdb"
FILES_gdb-dbg = "${bindir}/.debug/gdb"
RDEPENDS_gdb_append = " glibc-thread-db "
FILES_gdbserver = "${bindir}/gdbserver ${libdir}/bin/sysroot-gdbserver"
FILES_gdbserver-dbg = "${bindir}/.debug/gdbserver ${libdir}/bin/.debug/sysroot-gdbserver"
RDEPENDS_gdbserver_append = " glibc-thread-db "
FILES_libgomp = "${libdir}/libgomp.so.*"
FILES_libgomp-dev = "${libdir}/libgomp.so ${libdir}/gcc/${TARGET_SYS}/${CSL_VER_GCC}/include/omp.h"
FILES_libgomp-staticdev = "${libdir}/libgomp.a"
FILES_libgomp-dbg = "${libdir}/.debug/libgomp*"
FILES_libgcc = "${base_libdir}/libgcc_s.so.1"
FILES_libgcc-dbg += "${base_libdir}/.debug/libgcc_s.so.1 /usr/src/debug/gcc/libgcc /usr/src/debug/generated/gcc/*/libgcc /usr/src/debug/generated/gcc/*/*/libgcc ${base_libdir}/.debug/libgcc_s.so.1 ${libdir}/.debug/libssp*"
RDEPENDS_libgcc-dev = "eglibc-dev libgcc"
# This is needed to overcome a limitation in RPM multilib package selection
FILERDEPENDSFLIST_libgcc-dev_append = " ${base_libdir}/libgcc_s.so"
FILERDEPENDS_${base_libdir}/libgcc_s.so_libgcc-dev = " ${base_libdir}/libgcc_s.so.1"
# /usr/lib has to exist for the compiler to work.
# CSL_TARGET_CORE is the (possibly empty, if none's needed) directory
# containing the symlink back to the sysroot for a multilib.
FILES_libgcc-dev = "${base_libdir}/libgcc_s.so ${@'/' + CSL_TARGET_CORE if CSL_TARGET_CORE else ''} /lib /usr/lib /lib64 /usr/lib64"
FILES_libgcc-dev += "${libdir}/gcc/${TARGET_SYS}/${CSL_VER_GCC}"
INSANE_SKIP_${MLPREFIX}libgcc-dev += "staticdev"
FILES_libgcov-dev = "${libdir}/${TARGET_SYS}/${CSL_VER_GCC}/libgcov.a"
INSANE_SKIP_${MLPREFIX}libgcov-dev += "staticdev"
FILES_libssp = "${libdir}/libssp.so.0 ${libdir}/libssp.so.0.0.0"
FILES_libssp-dev = "${libdir}/libssp.la ${libdir}/libssp_nonshared.a ${libdir}/libssp_nonshared.la ${libdir}/libssp.so ${libdir}/gcc/${TARGET_SYS}/${CSL_VER_GCC}/include/ssp"
FILES_libssp-staticdev = "${libdir}/libssp.a"
FILES_linux-libc-headers = ""
# LIKELY_KERNEL_HEADERS is defined in sourcery-tc-shared.inc.
FILES_linux-libc-headers-dev = "${LIKELY_KERNEL_HEADERS}"

FILES_libstdc++ = "${libdir}/libstdc++.so.* ${datadir}/gdb/*"
FILES_libstdc++-dbg = "${libdir}/.debug/libstdc++*"
FILES_libstdc++-dev = "${includedir}/c++ \
	${libdir}/libstdc++.so \
	${libdir}/libstdc++.la \
	${libdir}/libsupc++.la"
RDEPENDS_libstdc++-dev += "eglibc-dev libstdc++"
# This is needed to overcome a limitation in RPM multilib package selection
FILERDEPENDSFLIST_libstdc++-dev_append = " ${libdir}/libstdc++.so"
FILERDEPENDS_${libdir}/libstdc++.so_libstdc++-dev = " ${libdir}/libstdc++.so.6.0.18"

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

USR_SUBDIRS = "bin ${baselib} libexec sbin share lib/locale lib/gconv"

toolchain_binary_install() {
        libc=$1
        libpath=$2
        tc_source="$libc${libpath:+/}$libpath"
        echo "tc_source: $tc_source ($libpath/${baselib} in $libc)"

        mkdir -p "${TOOLCHAIN_SYSROOT_COPY}"

        if [ -d $tc_source ] && [ -e $tc_source/usr/${baselib}/libc.so ]; then
                sysroot=$tc_source
                echo "Extracting prebuilt binaries from $tc_source."
                tccp $sysroot/${baselib}/. ${TOOLCHAIN_SYSROOT_COPY}/${base_libdir}
		if [ -n "${EXTERNAL_SOURCERY_EXTRA_SYSROOT_FILES}" ]; then
			for file in ${EXTERNAL_SOURCERY_EXTRA_SYSROOT_FILES}; do
				subdir="$(dirname $file)"
				tccp -R $sysroot/$file ${TOOLCHAIN_SYSROOT_COPY}/$subdir
			done
		fi
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
                cpioextract "$tc_source" ${TOOLCHAIN_SYSROOT_COPY} ${baselib}/\* etc/\* sbin/\* $subdirs ${EXTERNAL_SOURCERY_EXTRA_SYSROOT_FILES}
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
	# (prebuilt headers other than the kernheaders are over in
	# eglibc-sourcery-*.)
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
				# Not all of the kernel directories exist
				# in all sets of headers.
				if [ ! -d "${TOOLCHAIN_SYSROOT_COPY}${includedir}/$subsub" ]; then
					continue
				fi
				install -d ${D}${includedir}/$subsub
				tccp ${TOOLCHAIN_SYSROOT_COPY}${includedir}/$subsub/. ${D}${includedir}/$subsub
			done
		done
	fi
	tccp ${TOOLCHAIN_SYSROOT_COPY}${base_libdir}/libgcc* ${D}${base_libdir}
	tccp ${TOOLCHAIN_SYSROOT_COPY}${libdir}/libgomp* ${D}${libdir}
	tccp ${TOOLCHAIN_SYSROOT_COPY}${libdir}/libstdc++* ${D}${libdir}
	tccp ${TOOLCHAIN_SYSROOT_COPY}${libdir}/libssp* ${D}${libdir}
	
	external_toolchain_links ${D}

	# We also need other bits of libgcc-s-dev, such as the crtbegin.o, headers, etc.
	# We write them not in the sourcer-tc location, but to match the oe-core locations.
	# Doing that will allow us to build a target toolchain from source if needed.
	libgcc_src=`${CC} -print-file-name=crtbegin.o`
	libgcc_path=`dirname $libgcc_src`

	if [ "$libgcc_path" = '.' ]; then
		echo "ERROR: Unable to find crtbegin.o"
		echo $libgcc_src -- $libgcc_path
	fi

	libgcc_multidir=`${CC} -print-multi-directory`

	libgcc_srcdir=""
	if [ "$libgcc_multidir" != '.' ]; then
		libgcc_srcdir=`echo $libgcc_multidir | sed 's,[^/]*,..,g'`
	fi

	install -d ${D}${libdir}/${TARGET_SYS}/${CSL_VER_GCC}/
	cp $libgcc_path/crt*.o $libgcc_path/libgcc*.a $libgcc_path/libgcov.a \
		${D}${libdir}/${TARGET_SYS}/${CSL_VER_GCC}/.

	install -d ${D}${libdir}/gcc/${TARGET_SYS}/${CSL_VER_GCC}/include
	cp -r $libgcc_path/$libgcc_srcdir/include/omp.h \
	   $libgcc_path/$libgcc_srcdir/include/ssp \
	   ${D}${libdir}/gcc/${TARGET_SYS}/${CSL_VER_GCC}/include/.

	# We need to attempt to find the C++ headers
	# ${CPP} -Wp,-lang-c++,-v - 2>&1 < /dev/null || :
	for each in `${CPP} -Wp,-lang-c++,-v - 2>&1 < /dev/null | sed -n '/^ .*${CSL_VER_GCC}/p' || :` ; do
		echo "Checking for $each/c++..."
		if [ -d $each/c++/${CSL_VER_GCC} ]; then
			echo "Found C++ headers!"
			install -d ${D}${includedir}/c++
			cp -r $each/c++/${CSL_VER_GCC}/* ${D}${includedir}/c++/.
			src="${includedir}/c++/${CSL_TARGET_SYS}"
			dest="${includedir}/c++/${TARGET_SYS}"
			install -d ${D}$dest
			if [ "$src" != "$dest" ] || [ "$src" = "$dest" -a "$libgcc_multidir" != "." ]; then
				# Make sure if there was a previous 'bits' we remove it
				# so that we can copy in the correct contents for this system...
				rm -rf ${D}$dest/bits
				mv ${D}$src/$libgcc_multidir/bits ${D}$dest/.
			fi
			if [ "$src" != "$dest" ]; then
				# Cleanup leftover droppings...
				rm -rf ${D}$src
			fi
			break
		fi
	done

	if [ ! -d ${D}${includedir}/c++ ]; then
		echo "Unable to find/copy the C++ headers!"
		exit 1
	fi

	install -d ${D}${datadir}/gdb/auto-load
	if [ -f ${D}${libdir}/libstd*.py ]; then
		mv ${D}${libdir}/libstd*.py  ${D}${datadir}/gdb/auto-load
	fi

	if [ -n "${SOURCERY_KERNEL_HEADERS_TARBALL}" -a -f "${SOURCERY_KERNEL_HEADERS_TARBALL}" ]; then
		tar -C ${TOOLCHAIN_SYSROOT_COPY} -xjf ${SOURCERY_KERNEL_HEADERS_TARBALL}
	fi

}
