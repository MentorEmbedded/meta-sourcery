OE_IMPORTS += "oe.external"

EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"
EXTERNAL_TOOLCHAIN_LIBROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-file-name=crtbegin.o'])).rstrip().replace('/crtbegin.o', '')}"

EXTERNAL_INSTALL_SOURCE_PATHS = "\
    ${EXTERNAL_TOOLCHAIN_SYSROOT} \
    ${EXTERNAL_TOOLCHAIN}/${EXTERNAL_TARGET_SYS} \
    ${EXTERNAL_TOOLCHAIN_SYSROOT}/.. \
    ${EXTERNAL_TOOLCHAIN} \
    ${D} \
"

# Potential locations within the external toolchain sysroot
FILES_MIRRORS = "\
    ${bindir}/|/usr/${baselib}/bin/\n \
    ${base_libdir}/|/usr/${baselib}/\n \
    ${libexecdir}/|/usr/libexec/\n \
    ${libexecdir}/|/usr/${baselib}/${PN}\n \
    ${mandir}/|/usr/share/man/\n \
    ${mandir}/|/usr/man/\n \
    ${mandir}/|/man/\n \
    ${mandir}/|/share/doc/*-${EXTERNAL_TARGET_SYS}/man/\n \
    ${prefix}/|${base_prefix}/\n \
"

def external_run(d, *args):
    """Convenience wrapper"""
    oe_import(d)
    return oe.external.run(d, *args)
