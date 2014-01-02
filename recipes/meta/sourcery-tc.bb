#
# Copyright (C) 2012,2013 Wind River Systems, Inc.
#
include sourcery-tc-shared.inc

COMPATIBLE_HOST = '(i.86.*)-linux'

PV = "${CSL_VER_GCC}"

DESCRIPTION = "The Sourcery G++ binary toolchain for Wind River Linux.  This \
package replaces the binutils and gcc related packages."

INHIBIT_PACKAGE_STRIP = '1'
INHIBIT_PACKAGE_DEBUG_SPLIT = '1'

DEPENDS = "eglibc"

PROVIDES += " \
	${@base_conditional('PREFERRED_PROVIDER_binutils', BPN, 'binutils', '', d)} \
	${@base_conditional('PREFERRED_PROVIDER_gcc', BPN, 'gcc', '', d)}  \
	${@base_conditional('PREFERRED_PROVIDER_prelink', BPN, 'prelink', '', d)}  \
	"

# List generated from the prelink recipe
# Missing /etc/rpm/macros.prelink & /etc/prelink.conf
#
FILES_${PN}-prelink = " \
	/opt/windriver/toolchain/bin/*-prelink \
	/opt/windriver/toolchain/bin/*-execstack \
	/opt/windriver/toolchain/libexec/*-prelink-rtld \
	"

# List generated from the binutils recipe
FILES_${PN}-binutils = " \
	/opt/windriver/toolchain/bin/*-ar \
	/opt/windriver/toolchain/bin/*-c++filt \
	/opt/windriver/toolchain/bin/*-addr2line \
	/opt/windriver/toolchain/bin/*-ranlib \
	/opt/windriver/toolchain/bin/*-strip \
	/opt/windriver/toolchain/bin/*-readelf \
	/opt/windriver/toolchain/bin/*-strings \
	/opt/windriver/toolchain/bin/*-size \
	/opt/windriver/toolchain/bin/*-elfedit \
	/opt/windriver/toolchain/bin/*-ld.bfd \
	/opt/windriver/toolchain/bin/*-as \
	/opt/windriver/toolchain/bin/*-objcopy \
	/opt/windriver/toolchain/bin/*-gprof \
	/opt/windriver/toolchain/bin/*-objdump \
	/opt/windriver/toolchain/bin/*-ld \
	/opt/windriver/toolchain/bin/*-nm \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/ar \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/objcopy \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/objdump \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/nm \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/ld \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/ranlib \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/strip \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/bin/as \
	"

RDEPENDS_${PN}-gcc = "${PN}-gcc-mlib"

# Everything else is gcc...
FILES_${PN}-gcc = " \
	/opt/windriver/toolchain/* \
	"

FILES_${PN}-gcc-mlib = " \
	/opt/windriver/toolchain/lib/gcc/* \
	/opt/windriver/toolchain/libexec/gcc/* \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/lib/* \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/lib64/* \
	"

FILES_${PN}-dbg = " \
	/opt/windriver/toolchain/${CSL_TARGET_SYS}/src \
	"

ALLOW_EMPTY_${PN} = "1"
RDEPENDS_${PN} = "toolchain-wrappers ${PN}-prelink ${PN}-binutils ${PN}-gcc"
RPROVIDES_${PN} += " \
	prelink \
	binutils \
	gcc gcc-plugin g++ cpp gcov gcc-plugin-dev gcc-dev \
	"

PACKAGES = " \
	${PN}-dbg \
	${PN}-staticdev \
	${PN}-dev \
	${PN}-doc \
	${PN}-prelink \
	${PN}-binutils \
	${PN}-gcc-mlib \
	${PN}-gcc \
	${PN} \
	"	

do_install() {
	for dir in bin lib/gcc libexec/gcc share/doc ${CSL_TARGET_SYS}; do
		mkdir -p ${D}/opt/windriver/toolchain/$dir
	done
	tccp ${EXTERNAL_TOOLCHAIN}/bin/${CSL_TARGET_SYS}-* ${D}/opt/windriver/toolchain/bin/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/libiberty.* ${D}/opt/windriver/toolchain/lib/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/gcc/${CSL_TARGET_SYS} ${D}/opt/windriver/toolchain/lib/gcc/.
	# We want prelink-rtld, but in one release this was over in /sbin;
	# will revert this later.
	warn_or_fail=true
	tccp ${EXTERNAL_TOOLCHAIN}/libexec/${CSL_TARGET_SYS}-* ${D}/opt/windriver/toolchain/libexec/.
	tccp ${EXTERNAL_TOOLCHAIN}/sbin/${CSL_TARGET_SYS}-* ${D}/opt/windriver/toolchain/sbin/.
	warn_or_fail=false
	tccp ${EXTERNAL_TOOLCHAIN}/libexec/gcc/${CSL_TARGET_SYS} ${D}/opt/windriver/toolchain/libexec/gcc/.
	# Some toolchains will have doc components.
	if [ -d ${EXTERNAL_TOOLCHAIN}/share/doc/*-${CSL_TARGET_SYS} ]; then
		tccp ${EXTERNAL_TOOLCHAIN}/share/doc/*-${CSL_TARGET_SYS} ${D}/opt/windriver/toolchain/share/doc/.
	fi
	tccp ${EXTERNAL_TOOLCHAIN}/${CSL_TARGET_SYS} ${D}/opt/windriver/toolchain/.

	for each in ${D}/opt/windriver/toolchain/bin/${CSL_TARGET_SYS}-* ; do
	  target=`basename $each`
	  link=`echo $target | sed -e "s,${CSL_TARGET_SYS}-,${TARGET_SYS}-",`
	  ln -s $target ${D}/opt/windriver/toolchain/bin/$link
	done

	# Remove the libc directory
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/libc
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/libc.cpio.gz

	# Remove gdb, expat, mklibs, glibc
	rm -rf ${D}/opt/windriver/toolchain/bin/*-gdb
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/share/gdb
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/src/gdb
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/src/generated/gdb*
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/src/expat
	rm -rf ${D}/opt/windriver/toolchain/bin/*-mklibs
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/src/glibc
	rm -rf ${D}/opt/windriver/toolchain/${CSL_TARGET_SYS}/src/generated/glibc
}

# staticdev
INSANE_SKIP_${PN} = "ldflags already-stripped"
INSANE_SKIP_${PN}-prelink = "ldflags"
INSANE_SKIP_${PN}-binutils = "ldflags"
INSANE_SKIP_${PN}-gcc-mlib = "ldflags staticdev arch dev-so"
INSANE_SKIP_${PN}-gcc = "ldflags"
SKIP_FILEDEPS_${PN}-gcc-mlib = "1"

FILES_${PN}-staticdev = "/opt/windriver/toolchain/lib/*.a"
FILES_${PN}-doc = "/opt/windriver/toolchain/share/doc"

