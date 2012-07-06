require recipes-core/eglibc/eglibc.inc

TOOLCHAIN_OPTIONS = ""

# sourceryg++-${CSL_VER_MAIN}-${CSL_TARGET_SYS}.src.tar.bz2
CSL_SRC_URI ??= ""
SRC_URI = "${CSL_SRC_URI} \
           file://etc/ld.so.conf \
           file://generate-supported.mk"

LIC_FILES_CHKSUM = "file://LICENSES;md5=98a1128c4b58120182cbea3b1752d8b9 \
      file://COPYING;md5=393a5ca445f6965873eca0259a17f833 \
      file://posix/rxspencer/COPYRIGHT;md5=dc5485bb394a13b2332ec1c785f5d83a \
      file://COPYING.LIB;md5=bbb461211a33b134d42ed5ee802b37ff "

S = "${WORKDIR}/glibc-${PV}"
B = "${WORKDIR}/build-${TARGET_SYS}"
LIBC_VER_MAIN = "${@'-'.join(CSL_VER_MAIN.split('-')[:-1])}"
PV = "${CSL_VER_LIBC}-${LIBC_VER_MAIN}"
PR = "r0"

do_unpack[vardeps] += "unpack_libc"
do_unpack[postfuncs] += "unpack_libc"

unpack_libc () {
    rm -rf ${S}
    tar jxf */glibc-${CSL_VER_MAIN}.tar.bz2
    if tar jxf */glibc_ports-${CSL_VER_MAIN}.tar.bz2; then
        mv glibc-ports-${PV}/ ${S}/ports
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
                --with-headers=${STAGING_INCDIR} \
                --without-selinux \
                ${GLIBC_EXTRA_OECONF}"

EXTRA_OECONF += "${@get_libc_fpu_setting(bb, d)}"

do_configure () {
    CPPFLAGS="" oe_runconf
}

require recipes-core/eglibc/eglibc-package.inc
