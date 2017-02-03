GCC_VERSION := "${@oe.external.run(d, 'gcc', '-dumpversion').rstrip()}"
PV = "${GCC_VERSION}"
BINV = "${GCC_VERSION}"

require recipes-devtools/gcc/gcc-runtime.inc
inherit external-toolchain

# GCC >4.2 is GPLv3
DEPENDS = "libgcc"
EXTRA_OECONF = ""
COMPILERDEP = ""

python () {
    lic_deps = d.getVarFlag('do_populate_lic', 'depends', False)
    d.setVarFlag('do_populate_lic', 'depends', lic_deps.replace('gcc-source-${PV}:do_unpack', ''))
    cfg_deps = d.getVarFlag('do_configure', 'depends', False)
    d.setVarFlag('do_configure', 'depends', cfg_deps.replace('gcc-source-${PV}:do_preconfigure', ''))
}

target_libdir = "${libdir}"
HEADERS_MULTILIB_SUFFIX ?= "${@oe.external.run(d, 'gcc', *('${TARGET_CC_ARCH}'.split() + ['-print-sysroot-headers-suffix'])).rstrip()}"
external_libroot = "${@os.path.realpath('${EXTERNAL_TOOLCHAIN_LIBROOT}').replace(os.path.realpath('${EXTERNAL_TOOLCHAIN}') + '/', '/')}"
FILES_MIRRORS =. "\
    ${libdir}/gcc/${TARGET_SYS}/${BINV}/|${external_libroot}/\n \
    ${libdir}/gcc/${TARGET_SYS}/${BINV}/include/|/lib/gcc/${EXTERNAL_TARGET_SYS}/${BINV}/include/ \n \
    ${libdir}/gcc/${TARGET_SYS}/|${libdir}/gcc/${EXTERNAL_TARGET_SYS}/\n \
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
FILES_libgomp-dev += "\
    ${libdir}/gcc/${TARGET_SYS}/${BINV}/include/openacc.h \
"
BBCLASSEXTEND = ""

do_package[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
