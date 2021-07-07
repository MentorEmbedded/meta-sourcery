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

SRCREV = "4a871574351e40d298d65949426a502c0ecaffcc"

SRCBRANCH ?= "release/${PV}/master"

GLIBC_GIT_URI ?= "git://sourceware.org/git/glibc.git"
UPSTREAM_CHECK_GITTAGREGEX = "(?P<pver>\d+\.\d+(\.\d+)*)"

SRC_URI = "git://sourceware.org/git/glibc.git;branch=release/2.27/master;name=glibc \
          file://glibc_227_to_cb11.patch \
          file://glibc_2018.05-7_to_2018.05-17.patch \
          file://0010-eglibc-run-libm-err-tab.pl-with-specific-dirs-in-S.patch \
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

# glibc's utils need libgcc
do_package[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_ipk[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_deb[depends] += "${MLPREFIX}libgcc:do_packagedata"
do_package_write_rpm[depends] += "${MLPREFIX}libgcc:do_packagedata"

# glibc may need libssp for -fstack-protector builds
do_packagedata[depends] += "gcc-runtime:do_packagedata"
