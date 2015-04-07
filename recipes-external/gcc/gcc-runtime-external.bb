inherit external-toolchain

SUMMARY = "The GNU Compiler Collection - gcc runtime libraries"
HOMEPAGE = "http://www.gnu.org/software/gcc/"
SECTION = "devel"
GCC_VERSION := "${@external_run(d, 'gcc', '-dumpversion').rstrip()}"
PV = "${GCC_VERSION}"

DEPENDS += "libgcc"
PROVIDES += "virtual/${TARGET_PREFIX}compilerlibs"

PACKAGES =+ "\
    libstdc++ \
    libstdc++-dev \
    libstdc++-staticdev \
    libatomic \
    libatomic-dev \
    libatomic-staticdev \
    libasan \
    libasan-dev \
    libasan-staticdev \
    liblsan \
    liblsan-dev \
    liblsan-staticdev \
    libubsan \
    libubsan-dev \
    libubsan-staticdev \
    libtsan \
    libtsan-dev \
    libtsan-staticdev \
    libg2c \
    libg2c-dev \
    libg2c-staticdev \
    libfortran \
    libfortran-dev \
    libfortran-staticdev \
    libmudflap \
    libmudflap-dev \
    libmudflap-staticdev \
    libquadmath \
    libquadmath-dev \
    libquadmath-staticdev \
    libssp \
    libssp-dev \
    libssp-staticdev \
    libgomp \
    libgomp-dev \
    libgomp-staticdev \
    libitm \
    libitm-dev \
    libitm-staticdev \
"

SUMMARY_libitm = "The Transactional Memory runtime library"
SUMMARY_libitm-dev = "${SUMMARY_libitm} - development files"

FILES_libstdc++ = "${libdir}/libstdc++${SOLIBS}"
FILES_libstdc++-dev = "${libdir}/libstdc++${SOLIBSDEV} \
                       ${includedir}/c++/${GCC_VERSION}"
FILES_libstdc++-staticdev = "${libdir}/libstdc++.a \
                             ${libdir}/libsupc++.a"
FILES_libatomic = "${libdir}/libatomic${SOLIBS}"
FILES_libatomic-dev = "${libdir}/libatomic${SOLIBSDEV}"
FILES_libatomic-staticdev = "${libdir}/libatomic.a"
FILES_libasan = "${libdir}/libasan${SOLIBS}"
FILES_libasan-dev = "${libdir}/libasan${SOLIBSDEV}"
FILES_libasan-staticdev = "${libdir}/libasan.a"
FILES_liblsan = "${libdir}/liblsan${SOLIBS}"
FILES_liblsan-dev = "${libdir}/liblsan${SOLIBSDEV}"
FILES_liblsan-staticdev = "${libdir}/liblsan.a"
FILES_libubsan = "${libdir}/libubsan${SOLIBS}"
FILES_libubsan-dev = "${libdir}/libubsan${SOLIBSDEV}"
FILES_libubsan-staticdev = "${libdir}/libubsan.a"
FILES_libtsan = "${libdir}/libtsan${SOLIBS}"
FILES_libtsan-dev = "${libdir}/libtsan${SOLIBSDEV}"
FILES_libtsan-staticdev = "${libdir}/libtsan.a"
FILES_libg2c = "${libdir}/libg2c${SOLIBS}"
FILES_libg2c-dev = "${libdir}/libg2c${SOLIBSDEV}"
FILES_libg2c-staticdev = "${libdir}/libg2c.a"
FILES_libfortran = "${libdir}/libfortran${SOLIBS}"
FILES_libfortran-dev = "${libdir}/libfortran${SOLIBSDEV}"
FILES_libfortran-staticdev = "${libdir}/libfortran.a"
FILES_libmudflap = "${libdir}/libmudflap${SOLIBS}"
FILES_libmudflap-dev = "${libdir}/libmudflap${SOLIBSDEV}"
FILES_libmudflap-staticdev = "${libdir}/libmudflap.a"
FILES_libquadmath = "${libdir}/libquadmath${SOLIBS}"
FILES_libquadmath-dev = "${libdir}/libquadmath${SOLIBSDEV} \
                         ${libdir}/gcc/${EXTERNAL_TARGET_SYS}/${GCC_VERSION}/include/quadmath*"
FILES_libquadmath-staticdev = "${libdir}/libquadmath.a"
FILES_libssp = "${libdir}/libssp${SOLIBS}"
FILES_libssp-dev = "${libdir}/libssp${SOLIBSDEV} \
                    ${libdir}/gcc/${EXTERNAL_TARGET_SYS}/${GCC_VERSION}/include/ssp"
FILES_libssp-staticdev = "${libdir}/libssp.a"
FILES_libgomp = "${libdir}/libgomp${SOLIBS}"
FILES_libgomp-dev = "${libdir}/libgomp${SOLIBSDEV} \
                     ${libdir}/gcc/${EXTERNAL_TARGET_SYS}/${GCC_VERSION}/include/omp.h"
FILES_libgomp-staticdev = "${libdir}/libgomp.a"
FILES_libitm = "${libdir}/libitm${SOLIBS}"
FILES_libitm-dev = "${libdir}/libitm${SOLIBSDEV}"
FILES_libitm-staticdev = "${libdir}/libitm.a"

FILES_${PN}-dbg += "${datadir}/gdb/python/libstdcxx \
                    ${datadir}/gcc-${GCC_VERSION}/python/libstdcxx"

do_package[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_ipk[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_deb[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
do_package_write_rpm[depends] += "virtual/${MLPREFIX}libc:do_packagedata"
