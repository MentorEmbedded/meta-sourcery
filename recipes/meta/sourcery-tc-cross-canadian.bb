#
# Copyright (C) 2012,2013 Wind River Systems, Inc.
#
include sourcery-tc-shared.inc

inherit cross-canadian

INHIBIT_PACKAGE_STRIP = '1'
INHIBIT_PACKAGE_DEBUG_SPLIT = '1'
INHIBIT_DEFAULT_DEPS = '1'

SKIP_FILEDEPS = '1'

# Some things that we use in our standard tools are not useful in
# the SDK, as they're relevant only for filesystem installs.
OMITTED_IN_SDK = "mklibs prelink"

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
	# Not every toolchain has Python support for gdb, so copy these
	# parts in the warnings-only area.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/libpython2.* ${D}${prefix}/toolchain/lib/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/python2.7 ${D}${prefix}/toolchain/lib/.
	tccp ${EXTERNAL_TOOLCHAIN}/lib/python27.zip ${D}${prefix}/toolchain/lib/.
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

	if [ -n "${OMITTED_IN_SDK}" ]; then
		for file in ${OMITTED_IN_SDK}; do
			rm -f "${D}${bindir}/${CSL_TARGET_SYS}-$file"
		done
	fi

	external_toolchain_additional_links ${D}${bindir}
}

# staticdev
INSANE_SKIP_${PN} = "dev-so libdir staticdev"

FILES_${PN}-staticdev = "${prefix}/toolchain/lib/*.a"
FILES_${PN}-doc = "${prefix}/toolchain/share/doc"
