#
# Configuration to use external Sourcery G++ toolchain
#

EXTERNAL_TOOLCHAIN ?= "/usr/local/csl/${TARGET_ARCH}"

TOOLCHAIN_PATH_ADD = "${EXTERNAL_TOOLCHAIN}/bin:"
PATH =. "${TOOLCHAIN_PATH_ADD}"

CSL_TARGET_SYS_powerpc ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
CSL_TARGET_SYS_powerpc64 ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
CSL_TARGET_SYS_arm ?= "arm-none-linux-gnueabi arm-mentor-linux-gnueabi"
CSL_TARGET_SYS_mips ?= "mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_mipsel ?= "mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_mips64 ?= "mips64-nlm-linux-gnu mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_i686 ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
CSL_TARGET_SYS_i586 ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
CSL_TARGET_SYS = "${TARGET_SYS}"

def csl_target_sys(d):
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)

    for triplet in d.getVar('CSL_TARGET_SYS', True).split():
        gcc = os.path.join(toolchain_path, 'bin', triplet + '-gcc')
        if os.path.exists(gcc):
            return triplet + '-'
    return '${TARGET_SYS}-'

TARGET_PREFIX = "${@csl_target_sys(d)}"

PREFERRED_PROVIDER_linux-libc-headers = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-initial = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-intermediate = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}g++ = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}binutils = "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc ?= "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}compilerlibs = "external-sourcery-toolchain"
PREFERRED_PROVIDER_libgcc = "external-sourcery-toolchain"
PREFERRED_PROVIDER_eglibc ?= "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/libc ?= "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/libintl ?= "external-sourcery-toolchain"
PREFERRED_PROVIDER_virtual/libiconv ?= "external-sourcery-toolchain"
PREFERRED_PROVIDER_gdbserver = "external-sourcery-toolchain"

# No need to re-compile the locale files
GLIBC_INTERNAL_USE_BINARY_LOCALE = "precompiled"
ENABLE_BINARY_LOCALE_GENERATION = ""

TOOLCHAIN_OPTIONS = " --sysroot=${STAGING_DIR_HOST}"

def sourcery_get_sysroot(d):
    import subprocess
    sysroot_cmd = "${TARGET_PREFIX}gcc ${TARGET_CC_ARCH} -print-sysroot"
    try:
        toolchain_sysroot = bb.process.run(bb.data.expand(sysroot_cmd, d),
                                           stderr=subprocess.PIPE,
                                           env={"PATH": d.getVar('PATH', True)})[0].rstrip()
    except bb.process.CmdError as exc:
        bb.fatal(str(exc))
    else:
        return toolchain_sysroot

EXTERNAL_TOOLCHAIN_SYSROOT = "${@sourcery_get_sysroot(d)}"

CSL_IS_PRO = "${@'1' if os.path.exists('${EXTERNAL_TOOLCHAIN}/license') else '0'}"

TUNE_CCARGS_append_x86 = " ${@'-msgxx-glibc' if CSL_IS_PRO == '1' else ''}"
TUNE_CCARGS_append_x86-64 = " ${@'-msgxx-glibc' if CSL_IS_PRO == '1' else ''}"

# Unfortunately, the CSL ia32 toolchain has non-prefixed binaries in its
# bindir (e.g. gcc, ld). To avoid this messing up our build, we avoid adding
# this bindir to our PATH, and instead add symlinks to the prefixed binaries
# to our staging toolchain bindir.

ERROR_QA[type] ?= "list"
python toolchain_metadata_setup () {
    if not isinstance(e, bb.event.ConfigParsed):
        return

    import subprocess
    d = e.data

    l = d.createCopy()
    l.finalize()
    oe_import(l)

    if os.path.exists(bb.data.expand('${EXTERNAL_TOOLCHAIN}/bin/gcc', l)):
        d.setVar('TOOLCHAIN_PATH_ADD', '')
        populate_toolchain_links(l)

    # The external toolchain may not have been built with the yocto preferred
    # gnu hash setting, so ensure that the corresponding sanity check is a
    # warning, not an error.
    error_qa = oe.data.typed_value('ERROR_QA', l)
    if 'ldflags' in error_qa:
        error_qa.remove('ldflags')
        d.setVar('ERROR_QA', ' '.join(error_qa))
        d.appendVar('WARN_QA', ' ldflags')
}
addhandler toolchain_metadata_setup

def populate_toolchain_links(d):
    import errno
    import os
    from glob import glob

    pattern = d.expand('${EXTERNAL_TOOLCHAIN}/bin/${TARGET_PREFIX}*')
    files = glob(pattern)
    if not files:
        bb.fatal("Unable to populate toolchain binary symlinks in %s" % pattern)

    bindir = d.getVar('STAGING_BINDIR_TOOLCHAIN', True)
    bb.mkdirhier(bindir)
    for f in files:
        base = os.path.basename(f)
        newpath = os.path.join(bindir, base)
        try:
            os.symlink(f, newpath)
        except OSError as exc:
            if exc.errno == errno.EEXIST:
                break
            bb.fatal("Unable to populate toolchain binary symlink for %s: %s" % (newpath, exc))

require conf/distro/include/csl-versions.inc

BUILDCFG_VARS += "CSL_VER_MAIN"
