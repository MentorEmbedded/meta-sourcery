require recipes-core/glibc/glibc.inc
require recipes-external/glibc/glibc-external-version.inc

FILESPATH .= ":${COREBASE}/meta/recipes-core/glibc/glibc"
EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@external_run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"

EXTERNAL_PV_PREFIX ?= ""
EXTERNAL_PV_SUFFIX ?= ""
PV_prepend = "${@'${EXTERNAL_PV_PREFIX}' if '${EXTERNAL_PV_PREFIX}' else ''}"
PV_append = "${@'${EXTERNAL_PV_SUFFIX}' if '${EXTERNAL_PV_SUFFIX}' else ''}"

SRC_PV = "${@'-'.join('${PV}'.split('-')[:-1])}"

LIC_FILES_CHKSUM = "file://LICENSES;md5=e9a558e243b36d3209f380deb394b213 \
                    file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://posix/rxspencer/COPYRIGHT;md5=dc5485bb394a13b2332ec1c785f5d83a \
                    file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "\
    virtual/${TARGET_PREFIX}gcc \
    linux-libc-headers \
    bison-native \
"

PROVIDES += "glibc \
             virtual/${TARGET_PREFIX}libc-for-gcc \
             virtual/${TARGET_PREFIX}libc-initial \
             virtual/libc \
             virtual/libintl \
             virtual/libiconv"

TOOLCHAIN_OPTIONS = ""
HOST_CC_ARCH_remove = "--no-sysroot-suffix"

SRCREV ?= "b4108a361f05ee87122b86aa7d799e4512013588"

SRCBRANCH ?= "release/${PV}/master"

GLIBC_GIT_URI ?= "git://sourceware.org/git/glibc.git"
UPSTREAM_CHECK_GITTAGREGEX = "(?P<pver>\d+\.\d+(\.\d+)*)"

SRC_URI = "git://sourceware.org/git/glibc.git;branch=release/2.27/master;name=glibc \
          file://0001-Initialize-release-notes-for-new-glibc-version.patch \
          file://0002-Fix-uninitialized-variable-in-dynamic-linker.patch \
          file://0003-Install-extra-files-for-use-of-mklibs.patch \
          file://0004-Release-note-for-dropping-AF_BUS-patch.patch \
          file://0005-Work-around-Wclobbered-warning-from-pthread.h-ITS-15.patch \
          file://0006-powerpc-Fix-TLE-build-for-SPE-BZ-22926.patch \
          file://0007-sparc32-Add-nop-before-__startcontext-to-stop-unwind.patch \
          file://0008-NEWS-add-entries-for-bugs-22919-and-22926.patch \
          file://0009-manual-Document-missing-feature-test-macros.patch \
          file://0010-manual-Update-the-_ISOC99_SOURCE-description.patch \
          file://0011-Fix-a-typo-in-a-comment.patch \
          file://0012-Add-missing-reorder-end-in-LC_COLLATE-of-et_EE-BZ-22.patch \
          file://0013-powerpc-Undefine-Linux-ptrace-macros-that-conflict-w.patch \
          file://0014-linux-powerpc-sync-sys-ptrace.h-with-Linux-4.15-BZ-2.patch \
          file://0015-BZ-22342-Fix-netgroup-cache-keys.patch \
          file://0016-Fix-multiple-definitions-of-__nss_-_database-bug-229.patch \
          file://0017-i386-Fix-i386-sigaction-sa_restorer-initialization-B.patch \
          file://0018-Update-translations-from-the-Translation-Project.patch \
          file://0019-ca_ES-locale-Update-LC_TIME-bug-22848.patch \
          file://0020-lt_LT-locale-Update-abbreviated-month-names-bug-2293.patch \
          file://0021-Greek-el_CY-el_GR-locales-Introduce-ab_alt_mon-bug-2.patch \
          file://0022-cs_CZ-locale-Add-alternative-month-names-bug-22963.patch \
          file://0023-NEWS-Add-entries-for-bugs-22848-22932-22937-22963.patch \
          file://0024-Update-nios2-ULPs-file-for-glibc-2.27.patch \
          file://0025-RISC-V-Do-not-initialize-gp-in-TLS-macros.patch \
          file://0026-RISC-V-fmax-fmin-Handle-signalling-NaNs-correctly.patch \
          file://0027-Update-ChangeLog-for-BZ-22884-riscv-fmax-fmin.patch \
          file://0028-Fix-i386-memmove-issue-bug-22644.patch \
          file://0029-Linux-i386-tst-bz21269-triggers-SIGBUS-on-some-kerne.patch \
          file://0030-RISC-V-fix-struct-kernel_sigaction-to-match-the-kern.patch \
          file://0031-Add-tst-sigaction.c-to-test-BZ-23069.patch \
          file://0032-Fix-signed-integer-overflow-in-random_r-bug-17343.patch \
          file://0033-Fix-crash-in-resolver-on-memory-allocation-failure-b.patch \
          file://0034-getlogin_r-return-early-when-linux-sentinel-value-is.patch \
          file://0035-Update-RWF_SUPPORTED-for-Linux-kernel-4.16-BZ-22947.patch \
          file://0036-manual-Move-mbstouwcs-to-an-example-C-file.patch \
          file://0037-manual-Various-fixes-to-the-mbstouwcs-example-and-mb.patch \
          file://0038-resolv-Fully-initialize-struct-mmsghdr-in-send_dg-BZ.patch \
          file://0039-Add-PTRACE_SECCOMP_GET_METADATA-from-Linux-4.16-to-s.patch \
          file://0040-Fix-blocking-pthread_join.-BZ-23137.patch \
          file://0041-Fix-stack-overflow-with-huge-PT_NOTE-segment-BZ-2041.patch \
          file://0042-Fix-path-length-overflow-in-realpath-BZ-22786.patch \
          file://0043-NEWS-add-entries-for-bugs-17343-20419-22644-22786-22.patch \
          file://0044-gd_GB-Fix-typo-in-abbreviated-May-bug-23152.patch \
          file://0045-sunrpc-Remove-stray-exports-without-enable-obsolete-.patch \
          file://0046-Don-t-write-beyond-destination-in-__mempcpy_avx512_n.patch \
          file://0047-Add-a-test-case-for-BZ-23196.patch \
          file://0048-Add-references-to-CVE-2018-11236-CVE-2017-18269.patch \
          file://0049-NEWS-Move-security-lated-changes-before-bug-list.patch \
          file://0050-libio-Avoid-_allocate_buffer-_free_buffer-function-p.patch \
          file://0051-posix-Fix-posix_spawnp-to-not-execute-invalid-binari.patch \
          file://0052-Mention-BZ-23264-in-NEWS.patch \
          file://0010-eglibc-run-libm-err-tab.pl-with-specific-dirs-in-S.patch \
          \
          file://Sync-gnulib-regex-implementation.patch \
          file://CVE-2016-10739.patch \
          file://CVE-2018-19591.patch \
          file://CVE-2019-9169.patch \
          file://CVE-2020-1752.patch \
          file://CVE-2020-6096.patch \
          file://CVE-2020-10029.patch \
          \
          file://etc/ld.so.conf \
          file://generate-supported.mk \
          "

TUNE_CCARGS_mips := "${@oe.utils.str_filter_out('-march=mips32', '${TUNE_CCARGS}', d)}"
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
                --enable-obsolete-nsl \
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

require recipes-external/glibc/glibc-sysroot-setup.inc
require recipes-external/glibc/glibc-package-adjusted.inc

bberror_task-install () {
    # Silence any errors from oe_multilib_header, as we don't care about
    # missing multilib headers, as the oe-core glibc version isn't necessarily
    # the same as our own.
    :
}

linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"

do_install_append () {
    for dir in ${linux_include_subdirs}; do
        rm -rf "${D}${includedir}/$dir"
    done
}

# This should be dropped once it starts failing
# a patch has been submitted upstream already to
# the master branch for coping up with this.
do_poststash_install_cleanup_append () {
    if [ "${baselib}" != "lib" ]; then
        rmdir --ignore-fail-on-non-empty "${D}${prefix}/lib"
    fi
}

S = "${WORKDIR}/git"
B = "${WORKDIR}/build-${TARGET_SYS}"

python () {
    if not d.getVar("EXTERNAL_TOOLCHAIN", True):
        raise bb.parse.SkipPackage("External toolchain not configured (EXTERNAL_TOOLCHAIN not set).")
}
