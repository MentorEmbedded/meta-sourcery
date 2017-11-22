HOMEPAGE = "https://sourceware.org/newlib/"
SUMMARY = "C library for embedded systems"
DESCRIPTION = "Newlib is a conglomeration of several library parts, all under free software licenses that make them easily usable on embedded products."
LICENSE = "GPLv2 & LGPLv3 & GPLv3 & LGPLv2"
SECTION = ""
DEPENDS = ""
PROVIDES += "newlib virtual/libc virtual/${TARGET_PREFIX}libc-for-gcc"

inherit external-toolchain

FILES_${PN}-dev += "${includedir}/sys/*.h ${includedir}/machine/*.h ${includedir}/*.h"
FILES_${PN}-staticdev += "${libdir}/libc.a ${libdir}/libm.a ${libdir}/lib.a"
