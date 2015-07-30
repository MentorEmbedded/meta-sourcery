require recipes-core/glibc/glibc.inc


EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

EXTERNAL_PV_PREFIX ?= ""
EXTERNAL_PV_SUFFIX ?= ""
PV_prepend = "${@'${EXTERNAL_PV_PREFIX}' if '${EXTERNAL_PV_PREFIX}' else ''}"
PV_append = "${@'${EXTERNAL_PV_SUFFIX}' if '${EXTERNAL_PV_SUFFIX}' else ''}"

def get_external_libc_version(d):
    sysroot = d.getVar('EXTERNAL_TOOLCHAIN_SYSROOT', True)
    libpath = oe.path.join(sysroot, d.getVar('base_libdir', True))
    if os.path.exists(libpath):
        for filename in os.listdir(libpath):
            if filename.startswith('libc-'):
                return filename[5:-3]

    return 'UNKNOWN'

PV := "${@get_external_libc_version(d)}"
SRC_PV = "${@'-'.join('${PV}'.split('-')[:-1])}"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "\
    virtual/${TARGET_PREFIX}gcc \
    linux-libc-headers \
"
DEPENDS_remove = "libtool-cross make-native"

PROVIDES += "glibc \
             virtual/${TARGET_PREFIX}libc-for-gcc \
             virtual/${TARGET_PREFIX}libc-initial \
             virtual/libc \
             virtual/libintl \
             virtual/libiconv"

TOOLCHAIN_OPTIONS = ""

SOURCERY_SRC_URI ?= ""
SRC_URI = "${SOURCERY_SRC_URI} \
           file://etc/ld.so.conf \
           file://generate-supported.mk"

S = "${WORKDIR}/glibc-${SRC_PV}"
B = "${WORKDIR}/build-${TARGET_SYS}"

do_unpack[vardeps] += "unpack_libc"
do_unpack[postfuncs] += "unpack_libc"

unpack_libc () {
    rm -rf ${S}
    tar jxf */glibc-*.tar.bz2
    if tar jxf */glibc_ports-*.tar.bz2 2>/dev/null; then
        mv glibc-ports-${SRC_PV}/ ${S}/ports
    fi

    # Ensure that we can build with make 4.0 even with older glibc
    #sed -i -e '/critic_missing make/s/\(\[3\.79\*[^,]*\)\],/[\1 | 4\.0],/' ${S}/configure.in
    if [ -e "${S}/configure" ]; then
        sed -i -e 's/\(^ *3.79\*[^)]*\))/\1 | 4.0)/' ${S}/configure
    fi
}
unpack_libc[dirs] = "${WORKDIR}"

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

require recipes-external/glibc/glibc-sysroot-setup.inc
require recipes-external/glibc/glibc-package-adjusted.inc

python () {
    if not d.getVar("EXTERNAL_TOOLCHAIN"):
        raise bb.parse.SkipPackage("External toolchain not configured (EXTERNAL_TOOLCHAIN not set).")

    if not d.getVar("SOURCERY_SRC_URI"):
        raise bb.parse.SkipPackage("glibc-sourcery requires that SOURCERY_SRC_URI point to the sourcery source tarball")
}
