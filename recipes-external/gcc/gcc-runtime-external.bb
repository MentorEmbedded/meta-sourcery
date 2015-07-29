GCC_VERSION := "${@external_run(d, 'gcc', '-dumpversion').rstrip()}"
PV = "${GCC_VERSION}"
BINV = "${GCC_VERSION}"

require recipes-devtools/gcc/gcc-runtime.inc
inherit external-toolchain

# GCC >4.2 is GPLv3
LICENSE = "GPL-3.0-with-GCC-exception & GPLv3"
DEPENDS = "libgcc"
EXTRA_OECONF = ""
python () {
    gccs = d.expand('gcc-source-${PV}')

    lic_deps = d.getVarFlag('do_populate_lic', 'depends', True).split()
    d.setVarFlag('do_populate_lic', 'depends', ' '.join(filter(lambda d: d != '{}:do_unpack'.format(gccs), lic_deps)))

    cfg_deps = d.getVarFlag('do_configure', 'depends', True).split()
    d.setVarFlag('do_configure', 'depends', ' '.join(filter(lambda d: d != '{}:do_preconfigure'.format(gccs), cfg_deps)))
}

target_libdir = "${libdir}"
HEADERS_MULTILIB_SUFFIX ?= "${@external_run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot-headers-suffix'])).rstrip()}"
external_libroot = "${@os.path.realpath('${EXTERNAL_TOOLCHAIN_LIBROOT}').replace(os.path.realpath('${EXTERNAL_TOOLCHAIN}') + '/', '/')}"
FILES_MIRRORS =. "\
    ${libdir}/gcc/${TARGET_SYS}/${GCC_VERSION}/|${external_libroot}/\n \
    ${includedir}/c\+\+/${GCC_VERSION}/${TARGET_SYS}/|${includedir}/c++/${GCC_VERSION}/${EXTERNAL_TARGET_SYS}${HEADERS_MULTILIB_SUFFIX}/\n \
    ${includedir}/c\+\+/${GCC_VERSION}/${TARGET_SYS}/|${includedir}/c++/${GCC_VERSION}/${EXTERNAL_TARGET_SYS}/\n \
"

do_install_extra () {
    if [ "${TARGET_SYS}" != "${EXTERNAL_TARGET_SYS}" ]; then
        if [ -d "${D}${includedir}/c++/${GCC_VERSION}/${EXTERNAL_TARGET_SYS}" ]; then
            mv -v "${D}${includedir}/c++/${GCC_VERSION}/${EXTERNAL_TARGET_SYS}/." "${D}${includedir}/c++/${GCC_VERSION}/${TARGET_SYS}/"
        fi
    fi

    # Clear out the unused c++ header multilibs
    multilib="${HEADERS_MULTILIB_SUFFIX}"
    for path in ${D}${includedir}/c++/${GCC_VERSION}/${TARGET_SYS}/*; do
        case ${path##*/} in
            ${multilib#/})
                mv -v "$path/"* "${D}${includedir}/c++/${GCC_VERSION}/${TARGET_SYS}/"
                ;;
        esac
        rm -rfv "$path"
    done
}

FILES_${PN}-dbg += "${datadir}/gdb/python/libstdcxx"
FILES_libstdc++-dev = "\
    ${includedir}/c++ \
    ${libdir}/libstdc++.so \
    ${libdir}/libstdc++.la \
    ${libdir}/libsupc++.la \
"
BBCLASSEXTEND = ""

# libstdc++ needs glibc
do_package[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_ipk[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_deb[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_rpm[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
