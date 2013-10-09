require sourcery-tc-shared.inc

# Extract the multilib stuff before trying to do anything else fancy.
# (We need this for preliminary headers.)
do_configure[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"
do_install[depends] += "${EXTERNAL_SOURCERY_TOOLCHAIN_SETUP}"

require recipes-core/eglibc/eglibc.inc

DEPENDS += "sourcery-tc-prebuilt"

PROVIDES += "eglibc"

# sourceryg++-${CSL_VER_MAIN}-${CSL_TARGET_SYS}.src.tar.bz2
CSL_SRC_URI ?= "file://${CSL_SRC_FILE}"
SRC_URI = "${CSL_SRC_URI} \
	   file://etc/ld.so.conf \
           file://generate-supported.mk"

LIC_FILES_CHKSUM = "file://LICENSES;md5=e9a558e243b36d3209f380deb394b213 \
      file://COPYING;md5=393a5ca445f6965873eca0259a17f833 \
      file://posix/rxspencer/COPYRIGHT;md5=dc5485bb394a13b2332ec1c785f5d83a \
      file://COPYING.LIB;md5=bbb461211a33b134d42ed5ee802b37ff "

S = "${WORKDIR}/glibc-${PV}"
B = "${WORKDIR}/build-${TARGET_SYS}"
LIBC_VER_MAIN = "${@'-'.join(EXTERNAL_TOOLCHAIN_DROP.split('-')[:-1])}"
PV = "${CSL_VER_LIBC}-${LIBC_VER_MAIN}"
PR = "r1"

do_unpack[vardeps] += "unpack_libc"
do_unpack[postfuncs] += "unpack_libc"

unpack_libc () {
    rm -rf ${S}
    tar jxf */glibc-${EXTERNAL_TOOLCHAIN_DROP}.tar.bz2
    if [ ! -f ${S}/ChangeLog.eglibc ]; then
    	# It probably got unpacked somewhere else...
	set -- */ChangeLog.eglibc
	srcdir="${S}"
	case $# in
	1)	dir=${1%/*}
		echo "glibc-${EXTERNAL_TOOLCHAIN_DROP} source unpacked to $dir, moving it to ${srcdir##*/}."
		mv $dir $srcdir;;
	*)	echo >&2 "Error: glibc-${EXTERNAL_TOOLCHAIN_DROP} source did not unpack to ${srcdir##*/}."
		exit 1
		;;
	esac
    fi
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
		--enable-obsolete-rpc \
                ${GLIBC_EXTRA_OECONF}"

EXTRA_OECONF += "${@get_libc_fpu_setting(bb, d)}"

sourcery_glibc_fix_cflags() {
        export CC='${CSL_TARGET_SYS}-gcc ${TUNE_CCARGS}'
        orig_cflags='${CFLAGS}'
        case orig_cflags in
        *-O0*) 
                echo >&2 "WARNING: -O0 not supported for eglibc rebuild. Not using it.";;
        *-fstack-protector-all*)       
                echo >&2 "WARNING: -fstack-protector-all not supported for
                eglibc rebuild. Not using it.";;
        esac
        
        new_cflags="$(echo $orig_cflags | sed -e 's/ *-O0//g' -e 's/-fstack-protector-all//g') -g -O2"
        echo "glibc rebuild flag/compiler fixup:"
        echo "  Using CC: $CC"
        echo "  Original CFLAGS: ${CFLAGS}"
        echo "  Using CFLAGS: $new_cflags"
        export CFLAGS="$new_cflags"
}

do_compile_prepend() {
        sourcery_glibc_fix_cflags
}

do_configure_prepend() {
        sourcery_glibc_fix_cflags
        # You can't build glibc without some preliminary headers. We steal
        # them from the compiler.
        ( cd ${TOOLCHAIN_SYSROOT_COPY}/usr/include; find . ! -type d ) > ${B}/HEADER_LIST
	mkdir -p "${STAGING_DIR_TCBOOTSTRAP}/usr/include"
	mkdir -p "${STAGING_INCDIR}"
        ( cd ${STAGING_INCDIR}/usr/include; cpio -o < ${B}/HEADER_LIST > ${B}/headers_saved.cpio ) || true
	# Copy into both the sysroot and the "tcbootstrap" sysroot,
	# because they get used for different parts of the eglibc build.
        ( cd ${TOOLCHAIN_SYSROOT_COPY}/usr/include; cpio -o < ${B}/HEADER_LIST ) |
          (cd ${STAGING_DIR_TCBOOTSTRAP}/usr/include; cpio -id || true)
        ( cd ${TOOLCHAIN_SYSROOT_COPY}/usr/include; cpio -o < ${B}/HEADER_LIST ) |
          (cd ${STAGING_INCDIR}; cpio -id || true)
}

do_install_append() {
	# Or don't.
	# (cd ${STAGING_INCDIR}; xargs < ${B}/HEADER_LIST rm; cpio -id < ${B}/headers_saved.cpio) || true
        # info/dir needs to be regenerated filesystem-wide, if it's present
        # it can clash with other packages.
        rm -f ${D}${infodir}/dir
}

oe_runmake () {
    if [ "$1" = "config" ]; then
        return
    else
	${MAKE} ${EXTRA_OEMAKE} "$@"
    fi
}

do_configure () {
    CPPFLAGS="${TOOLCHAIN_OPTIONS}" oe_runconf
}

require eglibc-package-adjusted.inc

CSL_VER_MAIN ?= ""
CSL_VER_LIBC ?= ""

python () {
    if not d.getVar("CSL_VER_MAIN"):
	raise bb.parse.SkipPackage("External CSL toolchain not configured (CSL_VER_MAIN not set).")
}
