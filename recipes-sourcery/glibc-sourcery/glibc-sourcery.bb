require recipes-core/glibc/glibc.inc
require recipes-external/glibc/glibc-external-version.inc

EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

EXTERNAL_PV_PREFIX ?= ""
EXTERNAL_PV_SUFFIX ?= ""
PV_prepend = "${@'${EXTERNAL_PV_PREFIX}' if '${EXTERNAL_PV_PREFIX}' else ''}"
PV_append = "${@'${EXTERNAL_PV_SUFFIX}' if '${EXTERNAL_PV_SUFFIX}' else ''}"

SRC_PV = "${@'-'.join('${PV}'.split('-')[:-1])}"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "\
    virtual/${TARGET_PREFIX}gcc \
    linux-libc-headers \
"

PROVIDES += "glibc \
             virtual/${TARGET_PREFIX}libc-for-gcc \
             virtual/${TARGET_PREFIX}libc-initial \
             virtual/libc \
             virtual/libintl \
             virtual/libiconv"

TOOLCHAIN_OPTIONS = ""


SRCREV ?= "ea23815a795f72035262953dad5beb03e09c17dd"

SRCBRANCH ?= "release/${PV}/master"

GLIBC_GIT_URI ?= "git://sourceware.org/git/glibc.git"
UPSTREAM_CHECK_GITTAGREGEX = "(?P<pver>\d+\.\d+(\.\d+)*)"

SRC_URI = "git://sourceware.org/git/glibc.git;branch=release/2.24/master;name=glibc \
          file://0001-Add-release-note-for-update-to-glibc-2.24.patch \
          file://0002-Merge-VFP-ABI-dynamic-linker-compatibility-release-n.patch \
          file://0003-Merge-AF_BUS-changes.patch \
          file://0004-Fix-uninitialized-variable-in-dynamic-linker.patch \
          file://0005-Install-extra-files-for-use-of-mklibs.patch \
          file://0006-Add-release-note-for-increased-Linux-kernel-version-.patch \
          file://0007-powerpc-fix-ifunc-sel.h-with-GCC-6.patch \
          file://0008-powerpc-fix-ifunc-sel.h-fix-asm-constraints-and-clob.patch \
          file://0009-Fix-sNaN-handling-in-nearbyint-on-32-bit-sparc.patch \
          file://0010-sparc-remove-fdim-sparc-specific-implementations.patch \
          file://0011-Do-not-override-objects-in-libc.a-in-other-static-li.patch \
          file://0012-arm-mark-__startcontext-as-.cantunwind-bug-20435.patch \
          file://0013-argp-Do-not-override-GCC-keywords-with-macros-BZ-169.patch \
          file://0014-nptl-tst-once5-Reduce-time-to-expected-failure.patch \
          file://0015-CVE-2017-1000366.patch \
          file://0016-warning_variable_cancel_routine_might_be_clobbered.patch \
          file://etc/ld.so.conf \
          file://generate-supported.mk \
          "

TUNE_CCARGS_mips := "${@oe_filter_out('-march=mips32', '${TUNE_CCARGS}', d)}"
CPPFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
BUILD_CPPFLAGS = "-I${STAGING_INCDIR_NATIVE}"
TARGET_CPPFLAGS = "-I${STAGING_DIR_TARGET}${layout_includedir}"

export libc_cv_slibdir = "${base_libdir}"
EXTRA_OECONF = "--enable-kernel=${OLDEST_KERNEL} \
                --without-cvs --disable-profile --disable-debug --without-gd \
                --enable-clocale=gnu \
                --enable-add-ons \
                --enable-obsolete-rpc \
                --with-headers=${STAGING_INCDIR} \
                --without-selinux \
                ${GLIBC_EXTRA_OECONF}"

EXTRA_OECONF += "${@get_libc_fpu_setting(bb, d)}"

# Without 0005-fsl-e500-e5500-e6500-603e-fsqrt-implementation.patch from
# oe-core, this argument will break e6500 builds. The Sourcery G++ toolchain
# does not include this patch at this time.
GLIBC_EXTRA_OECONF_remove = "--with-cpu=e6500"

oe_runmake () {
    if [ "$1" = "config" ]; then
        return
    else
	${MAKE} ${EXTRA_OEMAKE} "$@"
    fi
}

do_configure () {
    CPPFLAGS="" oe_runconf
}

linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"

do_install_append () {
    for dir in ${linux_include_subdirs}; do
        rm -rf "${D}${includedir}/$dir"
    done
}

bberror_task-install () {
    # Silence any errors from oe_multilib_header, as we don't care about
    # missing multilib headers, as the oe-core glibc version isn't necessarily
    # the same as our own.
    :
}

require recipes-external/glibc/glibc-sysroot-setup.inc
require recipes-external/glibc/glibc-package-adjusted.inc

S = "${WORKDIR}/git"
B = "${WORKDIR}/build-${TARGET_SYS}"

RDEPENDS_tzcode += "bash"

python () {
    if not d.getVar("EXTERNAL_TOOLCHAIN", True):
        raise bb.parse.SkipPackage("External toolchain not configured (EXTERNAL_TOOLCHAIN not set).")
    
    #removing oe_multilib_header bits/syscall.h from do_install
    install = d.getVar('do_install', False)
    d.setVar('do_install', install.replace('oe_multilib_header bits/syscall.h bits/long-double.h', ''));

}
