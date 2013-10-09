#
# Copyright (C) 2012,2013 Wind River Systems, Inc.
#
include sourcery-tc-shared.inc

inherit cross-canadian

INHIBIT_PACKAGE_STRIP = '1'
INHIBIT_PACKAGE_DEBUG_SPLIT = '1'
INHIBIT_DEFAULT_DEPS = '1'

SKIP_FILEDEPS = '1'

PN = "sourcery-tc-cross-canadian-${TRANSLATED_TARGET_ARCH}"
BPN = "sourcery-tc"

PROVIDES += " \
	${PN}-dev \
	binutils-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	gcc-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	gdb-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	"

RPROVIDES_${PN} += " \
	binutils-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	gcc-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	gdb-cross-canadian-${TRANSLATED_TARGET_ARCH} \
	"

do_install() {
	for dir in bin lib/gcc libexec/gcc share/doc ${CSL_TARGET_SYS}; do
		mkdir -p ${D}${prefix}/toolchain/$dir
	done
	tccp ${EXTERNAL_TOOLCHAIN}/bin/${CSL_TARGET_SYS}-* ${D}${prefix}/toolchain/bin/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/libiberty.* ${D}${prefix}/toolchain/lib/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/gcc/${CSL_TARGET_SYS} ${D}${prefix}/toolchain/lib/gcc/.
	# We want prelink-rtld, but in one release this was over in /sbin;
	# will revert this later.
	warn_or_fail=true
	tccp ${EXTERNAL_TOOLCHAIN}/libexec/${CSL_TARGET_SYS}-* ${D}${prefix}/toolchain/libexec/.
	tccp ${EXTERNAL_TOOLCHAIN}/sbin/${CSL_TARGET_SYS}-* ${D}${prefix}/toolchain/sbin/.
	warn_or_fail=false
	tccp ${EXTERNAL_TOOLCHAIN}/libexec/gcc/${CSL_TARGET_SYS} ${D}${prefix}/toolchain/libexec/gcc/.
	# Some toolchains will have doc components.
	if [ -d ${EXTERNAL_TOOLCHAIN}/share/doc/*-${CSL_TARGET_SYS} ]; then
		tccp ${EXTERNAL_TOOLCHAIN}/share/doc/*-${CSL_TARGET_SYS} ${D}${prefix}/toolchain/share/doc/.
	fi
	tccp ${EXTERNAL_TOOLCHAIN}/${CSL_TARGET_SYS} ${D}${prefix}/toolchain/.

	# Remove the libc directory
	rm -rf ${D}${prefix}/toolchain/${CSL_TARGET_SYS}/libc

	mkdir -p ${D}${bindir}
	# Setup bin links... ${bindir}/... -> ${prefix}/toolchain/bin/...
	for each in ${D}${prefix}/toolchain/bin/${CSL_TARGET_SYS}-* ; do
	  target=../../toolchain/bin/`basename $each`
	  link=${D}${bindir}/`basename $each`
	  ln -s $target $link
	done

	# Copy in the get_feature if necessary
	if [ -e ${EXTERNAL_TOOLCHAIN}/bin/get_feature ]; then
	  tccp ${EXTERNAL_TOOLCHAIN}/bin/get_feature ${D}${bindir}/.
	fi

	# Setup gnuspe links on powerpc...
	if [ ${CSL_TARGET_SYS}- = "powerpc-*-linux-gnu-" ]; then
	  for each in ${D}${bindir}/${CSL_TARGET_SYS}-* ; do
	    target=`basename $each`
	    link=`echo $each | sed "s,linux-gnu-,linux-gnuespe-,"`
	    ln -s $target $link
	  done
	fi
}

# staticdev
INSANE_SKIP_${PN} = "dev-so staticdev"

FILES_${PN}-staticdev = "${prefix}/toolchain/lib/*.a"
FILES_${PN}-doc = "${prefix}/toolchain/share/doc"
