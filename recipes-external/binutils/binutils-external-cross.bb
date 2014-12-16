inherit external-toolchain-cross

SUMMARY = "GNU binary utilities"
HOMEPAGE = "http://www.gnu.org/software/binutils/"
BUGTRACKER = "http://sourceware.org/bugzilla/"
SECTION = "devel"
PN .= "-${TARGET_ARCH}"
PV := "${@external_run(d, 'ld', '-v').splitlines()[0].split()[-1].rstrip()}"
LICENSE = "${@'GPLv3' if '${PV}'.split('.') > '2.17.50.0.12'.split('.') else 'GPLv2'}"

PROVIDES += "\
    ${@'${PN}'.replace('-${TARGET_ARCH}', '')} \
    virtual/${TARGET_PREFIX}binutils \
"

EXTERNAL_CROSS_BINARIES = "ar as ld nm objcopy objdump ranlib strip \
                           addr2line c++filt elfedit gprof readelf size \
                           strings"

do_install_append () {
    ln -s ${TARGET_PREFIX}ld ${D}${bindir}/${TARGET_PREFIX}ld.bfd
}
