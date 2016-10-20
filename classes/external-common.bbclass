OE_IMPORTS += "oe.external"

EXTERNAL_TOOLCHAIN_SYSROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-sysroot'])).rstrip()}"
EXTERNAL_TOOLCHAIN_LIBROOT ?= "${@oe.external.run(d, 'gcc', *(TARGET_CC_ARCH.split() + ['-print-file-name=crtbegin.o'])).rstrip().replace('/crtbegin.o', '')}"
EXTERNAL_LIBC_KERNEL_VERSION ?= "${@external_get_kernel_version("${EXTERNAL_TOOLCHAIN_SYSROOT}${prefix}")}"

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

def external_get_kernel_version(p):
    import re
    for fn in ['include/linux/utsrelease.h', 'include/generated/utsrelease.h',
               'include/linux/version.h']:
        fn = os.path.join(p, fn)
        if os.path.exists(fn):
            break
    else:
        return ''

    try:
        f = open(fn)
    except IOError:
        pass
    else:
        with f:
            lines = f.readlines()

        for line in lines:
            m = re.match(r'#define LINUX_VERSION_CODE (\d+)$', line)
            if m:
                code = int(m.group(1))
                a = code >> 16
                b = (code >> 8) & 0xFF
                return '%d.%d' % (a, b)

    return ''
