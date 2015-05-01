SRC_URI = "file://SUPPORTED"

require recipes-core/glibc/glibc-common.inc
inherit external-toolchain

def get_external_libc_version(d):
    sysroot = d.getVar('EXTERNAL_TOOLCHAIN_SYSROOT', True)
    libpath = os.path.join(sysroot, 'lib')
    if os.path.exists(libpath):
        for filename in os.listdir(libpath):
            if filename.startswith('libc-'):
                return filename[5:-3]

    return 'UNKNOWN'

PV := "${@get_external_libc_version(d)}"

DEPENDS += "virtual/${TARGET_PREFIX}binutils \
            linux-libc-headers"
PROVIDES += "glibc \
             virtual/${TARGET_PREFIX}libc-for-gcc \
             virtual/${TARGET_PREFIX}libc-initial \
             virtual/libc \
             virtual/libintl \
             virtual/libiconv"

def get_external_libc_license(d):
    sysroot = d.getVar('EXTERNAL_TOOLCHAIN_SYSROOT', True)
    incpath = os.path.join(sysroot, d.getVar('includedir', True)[1:])
    errnopath = os.path.join(incpath, 'errno.h')

    with open(errnopath, 'rU') as f:
        text = f.read()

    lictext = """   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version."""

    if lictext in text:
        return 'LGPL-2.1+'

    return 'UNKNOWN'

LICENSE := "${@get_external_libc_license(d)}"

require recipes-external/glibc/glibc-sysroot-setup.inc
require recipes-external/glibc/glibc-package-adjusted.inc

FILES_MIRRORS .= "\
    ${base_sbindir}/|/usr/bin/ \n\
    ${base_sbindir}/|/usr/${baselib}/bin/ \n\
    ${sbindir}/|/usr/bin/ \n\
    ${sbindir}/|/usr/${baselib}/bin/ \n\
"

python do_install () {
    bb.build.exec_func('external_toolchain_do_install', d)
    bb.build.exec_func('glibc_external_do_install_extra', d)
}

glibc_external_do_install_extra () {
    mkdir -p ${D}${sysconfdir}
    touch ${D}${sysconfdir}/ld.so.conf

    if [ ! -e ${D}${libdir}/libc.so ]; then
        bbfatal "Unable to locate installed libc.so file (${libdir}/libc.so)." \
                "This may mean that your external toolchain uses a different" \
                "multi-lib setup than your machine configuration"
    fi
}

EXTERNAL_EXTRA_FILES += "\
    ${datadir}/i18n \
    ${libdir}/gconv \
    ${localedir} \
"

# These files are picked up out of the sysroot by glibc-locale, so we don't
# need to keep them around ourselves.
do_install_locale_append() {
	rm -rf ${D}${localedir}
}

python () {
    # Undo the do_install_append which joined shell to python
    install = d.getVar('do_install', False)
    python, shell = install.split('rm -f ', 1)
    d.setVar('do_install_glibc', 'rm -f ' + shell)
    d.setVarFlag('do_install_glibc', 'func', '1')
    new_install = python + '\n    bb.build.exec_func("do_install_glibc", d)\n'
    d.setVar('do_install', new_install.replace('\t', '    '))

    # Ensure that we pick up just libm, not all libs that start with m
    baselibs = d.getVar('libc_baselibs', False)
    baselibs.replace('${base_libdir}/libm*.so.*', '${base_libdir}/libm.so.*')
    d.setVar('libc_baselibs', baselibs)
}

# Default pattern is too greedy
FILES_${PN}-utils = "\
    ${bindir}/gencat \
    ${bindir}/getconf \
    ${bindir}/getent \
    ${bindir}/iconv \
    ${sbindir}/iconvconfig \
    ${bindir}/lddlibc4 \
    ${bindir}/locale \
    ${bindir}/makedb \
    ${bindir}/pcprofiledump \
    ${bindir}/pldd \
    ${bindir}/rpcgen \
    ${bindir}/sprof \
"
FILES_${PN}-doc += "${infodir}/libc.info*"

# Extract for use by do_install_locale
FILES_${PN} += "\
    ${bindir}/localedef \
    ${libdir}/gconv \
    ${libdir}/locale \
    ${datadir}/locale \
    ${datadir}/i18n \
"

FILES_${PN}-dev_remove := "${datadir}/aclocal"

FILES_${PN}-dev_remove = "/lib/*.o"
FILES_${PN}-dev += "${libdir}/*crt*.o"

libc_baselibs_dev += "${@' '.join('${libdir}/' + os.path.basename(l.replace('${SOLIBS}', '${SOLIBSDEV}')) for l in '${libc_baselibs}'.replace('${base_libdir}/ld*${SOLIBS}', '').split() if l.endswith('${SOLIBS}'))}"
FILES_${PN}-staticdev = "\
    ${@'${libc_baselibs_dev}'.replace('${SOLIBSDEV}', '.a')} \
    ${libdir}/libg.a \
    ${libdir}/libieee.a \
    ${libdir}/libmcheck.a \
    ${libdir}/librpcsvc.a \
"

FILES_${PN}-dev += "\
    ${libc_baselibs_dev} \
    ${libdir}/libcidn${SOLIBSDEV} \
    ${libdir}/libthread_db${SOLIBSDEV} \
    ${libdir}/libpthread${SOLIBSDEV} \
"
libc_headers_file = "${@bb.utils.which('${FILESPATH}', 'libc.headers')}"
FILES_${PN}-dev += "\
    ${@' '.join('${includedir}/' + f.rstrip() for f in base_read_file('${libc_headers_file}').splitlines())} \
    ${includedir}/fpu_control.h \
    ${includedir}/stdc-predef.h \
    ${includedir}/uchar.h \
"
FILES_${PN}-dev[file-checksums] += "${libc_headers_file}"

# Currently, ldd and tzcode from Sourcery G++ still have #!/bin/bash
RDEPENDS_ldd += "bash"
RDEPENDS_tzcode += "bash"
