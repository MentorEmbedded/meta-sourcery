#
# Configuration to use external Sourcery G++ toolchain
#

EXTERNAL_TOOLCHAIN ?= "UNDEFINED"

# Ensure that pseudo builds both 32 bit and 64 bit binaries, as we need the
# 32 bit pseudo to execute the 32 bit external toolchain binaries without
# errors or warnings.
NO32LIBS ?= "0"

# Ensure that we only attempt to package up locales which are available in the
# external toolchain. In the future, we should examine the external toolchain
# sysroot and determine this accurately.
GLIBC_GENERATE_LOCALES_remove = "en_US.UTF-8"

# No need to re-compile the locale files
GLIBC_INTERNAL_USE_BINARY_LOCALE ?= "precompiled"
ENABLE_BINARY_LOCALE_GENERATION = ""

# Prefer our recipes which extract files from the external toolchain
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc ?= "gcc-external-cross-${TARGET_ARCH}"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-initial ?= "gcc-external-cross-${TARGET_ARCH}"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-intermediate ?= "gcc-external-cross-${TARGET_ARCH}"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}g++ ?= "gcc-external-cross-${TARGET_ARCH}"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}binutils ?= "binutils-external-cross-${TARGET_ARCH}"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}compilerlibs ?= "gcc-runtime-external"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc ?= "glibc-external"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-initial ?= "glibc-external"
PREFERRED_PROVIDER_gcc-sanitizers ?= "gcc-sanitizers-external"
PREFERRED_PROVIDER_glibc ?= "glibc-external"
PREFERRED_PROVIDER_virtual/libc ?= "glibc-external"
PREFERRED_PROVIDER_virtual/libintl ?= "glibc-external"
PREFERRED_PROVIDER_virtual/libiconv ?= "glibc-external"

PREFERRED_PROVIDER_gdbserver ??= "gdbserver-external"
PREFERRED_PROVIDER_oprofile ??= "oprofile"

# These are defined in default-providers.inc, which is parsed before the
# tcmode, so we can't use ?= for them.
PREFERRED_PROVIDER_libgcc = "libgcc-external"
PREFERRED_PROVIDER_linux-libc-headers = "linux-libc-headers-external"

# Support use of an external toolchain with the SDK/ADE/etc
TOOLCHAIN_HOST_TASK_append = " nativesdk-external-script"

# Pull in our utility functions for use elsewhere
INHERIT += "external-common"

# Ensure that we don't pull in any internal toolchain recipes
INHERIT += "blacklist blacklist_dynamic"
PNBLACKLIST[uclibc] = "not building with an external toolchain"
PNBLACKLIST[uclibc-initial] = "not building with an external toolchain"
PNBLACKLIST[glibc] = "not building with an external toolchain"
PNBLACKLIST[glibc-initial] = "not building with an external toolchain"
PNBLACKLIST[glibc-intermediate] = "not building with an external toolchain"
PNBLACKLIST[gcc-runtime] = "not building with an external toolchain"
PNBLACKLIST[gcc-sanitizers] = "not building with an external toolchain"
PNBLACKLIST[gcc-source] = "not building with an external toolchain"
PNBLACKLIST[libgcc-initial] = "not building with an external toolchain"
PNBLACKLIST[libgcc] = "not building with an external toolchain"
PNBLACKLIST[linux-libc-headers] = "not building with an external toolchain"
PNBLACKLIST[linux-libc-headers-yocto] = "not building with an external toolchain"
PNBLACKLIST[external-sourcery-toolchain] = "using meta-sourcery, not poky's external toolchain"

PNBLACKLIST_DYNAMIC += "\
    ${MLPREFIX}gcc-cross-${TARGET_ARCH} \
    ${MLPREFIX}gcc-cross-initial-${TARGET_ARCH} \
    ${MLPREFIX}binutils-cross-${TARGET_ARCH} \
    \
    gcc-source-${@'${GCCVERSION}'.replace('%', '')} \
"

# Determine the prefixes to check for based on the target architecture (before
# any classes alter TARGET_ARCH)
EXTERNAL_TARGET_SYSTEMS[powerpc] ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[powerpc64] ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[arm] ?= "arm-none-linux-gnueabi arm-mentor-linux-gnueabi"
EXTERNAL_TARGET_SYSTEMS[aarch64] ?= "aarch64-linux-gnu aarch64-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mips] ?= "mips-linux-gnu mips-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mipsel] ?= "mips-linux-gnu mips-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mips64] ?= "mips64-nlm-linux-gnu mips-linux-gnu mips-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[x86_64] ?= "i686-pc-linux-gnu i686-mentor-linux-gnu x86_64-linux-gnu x86_64-amd-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[i686] ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[i586] ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
EXTERNAL_TARGET_SYSTEMS = "${TARGET_SYS}"

def external_target_sys(d):
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)

    for triplet in d.getVar('EXTERNAL_TARGET_SYSTEMS', True).split():
        gcc = os.path.join(toolchain_path, 'bin', triplet + '-gcc')
        if os.path.exists(gcc):
            return triplet
    return '${TARGET_SYS}'

# We need our -cross recipes to rebuild when the external toolchain changes,
# to recreate the links / wrapper scripts
BB_HASHBASE_WHITELIST_remove = "EXTERNAL_TOOLCHAIN"

# All we care about for the signatures is the result, not how we got there, so
# we don't want EXTERNAL_TARGET_SYSTEMS included.
EXTERNAL_TARGET_SYS ?= "${@external_target_sys(d)}"
EXTERNAL_TARGET_SYS[vardepvalue] = "${EXTERNAL_TARGET_SYS}"
EXTERNAL_TARGET_SYS[vardepsexclude] += "EXTERNAL_TARGET_SYSTEMS EXTERNAL_TOOLCHAIN"
TARGET_PREFIX = "${EXTERNAL_TARGET_SYS}-"

# TOOLCHAIN_OPTIONS would seem more appropriate, but that gets added to LD as
# well, and --no-sysroot-suffix only works for gcc, not binutils.
HOST_CC_ARCH += "--no-sysroot-suffix"

CSL_IS_PRO = "${@'1' if os.path.exists('${EXTERNAL_TOOLCHAIN}/license') else '0'}"

LDEMULATION = ""
LDEMULATION_ENDIAN = "${@'bt' if 'bigendian' in '${TUNE_FEATURES}'.split() else 'lt'}"
LDEMULATION_BITS = "${@'64' if 'n64' in '${TUNE_FEATURES}'.split() else '32'}"
LDEMULATION_mips64 = "elf${LDEMULATION_BITS}${LDEMULATION_ENDIAN}smip${@bb.utils.contains('TUNE_FEATURES', 'n32', 'n32', '', d)}"
TUNE_LDARGS += "${@'-m ${LDEMULATION}' if '${LDEMULATION}' else ''}"

# Ensure that the licensing variables are available to the toolchain.
export MGLS_LICENSE_FILE
export LM_LICENSE_FILE

python toolchain_metadata_setup () {
    import subprocess
    import tempfile

    d = e.data

    # Ensure that changes to toolchain licensing don't affect checksums
    d.appendVar('BB_HASHBASE_WHITELIST', ' MGLS_LICENSE_FILE LM_LICENSE_FILE')

    l = d.createCopy()
    l.finalize()

    systems = l.getVarFlag('EXTERNAL_TARGET_SYSTEMS', l.getVar('TARGET_ARCH', True), True)
    if systems:
        d.setVar('EXTERNAL_TARGET_SYSTEMS', systems)

    # Remove already-added toolchain install paths from the PATH, as they can
    # break the build (in particular, the ia32 toolchain, as it provdes
    # non-prefixed binaries).
    path = d.getVar('PATH', False).split(':')
    path = filter(lambda p: not p.endswith('/codebench/bin'), path)
    d.setVar('PATH', ':'.join(path))

    # The external toolchain may not have been built with the yocto preferred
    # gnu hash setting, so ensure that the corresponding sanity check is a
    # warning, not an error.
    error_qa = (l.getVar('ERROR_QA', True) or '').split()
    if 'ldflags' in error_qa:
        error_qa.remove('ldflags')
        d.setVar('ERROR_QA', ' '.join(error_qa))
        d.appendVar('WARN_QA', ' ldflags')

    # Determine if '-msgxx-glibc' is a valid toolchain option.
    # If so then we need to use it to ensure that the libraries included with
    # the toolchain are used rather than the build host native libraries.
    with tempfile.NamedTemporaryFile(suffix='.c') as f:
        try:
            subprocess.check_output([d.expand('${EXTERNAL_TOOLCHAIN}/bin/${EXTERNAL_TARGET_SYS}-gcc'), '-msgxx-glibc', '-E', f.name], cwd=d.getVar('TOPDIR', True), stderr=subprocess.STDOUT)
        except (OSError, subprocess.CalledProcessError):
            pass
        else:
            d.appendVar('TUNE_CCARGS', ' -msgxx-glibc')
}
toolchain_metadata_setup[eventmask] = "bb.event.ConfigParsed"
addhandler toolchain_metadata_setup

python toolchain_sanity_check () {
    d = e.data
    external_toolchain = d.getVar('EXTERNAL_TOOLCHAIN', True)
    if not external_toolchain or external_toolchain == 'UNDEFINED':
        bb.fatal("Error: EXTERNAL_TOOLCHAIN must be set to the path to your sourcery toolchain")

    if not os.path.exists(external_toolchain):
        bb.fatal("Error: EXTERNAL_TOOLCHAIN path '%s' does not exist" % external_toolchain)

    gccpath = os.path.join(external_toolchain, 'bin', e.data.expand('${EXTERNAL_TARGET_SYS}-gcc'))
    if not os.path.exists(gccpath):
        if (os.path.exists(os.path.dirname(gccpath)) and
            d.getVar('EXTERNAL_TARGET_SYS', True) == d.getVar('TARGET_SYS', True)):
            bb.warn("EXTERNAL_TARGET_SYS == TARGET_SYS. This indicates that the prefixes specified by EXTERNAL_TARGET_SYSTEMS were not found.")
        bb.fatal("Error: EXTERNAL_TOOLCHAIN gcc path '%s' does not exist" % gccpath)

    if d.getVar('GCC_VERSION', True) == 'UNKNOWN':
        bb.warn("EXTERNAL_TOOLCHAIN gcc version extraction failed, see debug messages for details")
}

# This runs at TreeDataPreparationStarted time, as we want bitbake -e to work,
# so it has to run after ConfigParsed, and we want to see these errors rather
# than the highly verbose unbuildable -external recipe errors which occur when
# generating the runqueue, so we need to run before BuildStarted.
toolchain_sanity_check[eventmask] = "bb.event.TreeDataPreparationStarted"
addhandler toolchain_sanity_check

GCCVERSION ?= "${@'.'.join('${GCC_VERSION}'.split('.')[:2])}%"
GCC_VERSION = "${@external_run(d, 'gcc', '-dumpversion').rstrip()}"
GCC_VERSION_allarch = ""
GCC_VERSION[vardepvalue] = "${GCC_VERSION}"

def sourcery_version(d):
    version = oe.external.run(d, 'gcc', '-v').splitlines()[-1]
    if version != 'UNKNOWN':
        main_version = version.split()[-1].rstrip(')')
        if main_version == 'Preview':
            main_version = version.split()[-3]
        return main_version
    else:
        return version

SOURCERY_VERSION = "${@sourcery_version(d)}"
SOURCERY_VERSION_allarch = ""
SOURCERY_VERSION[vardepvalue] = "${SOURCERY_VERSION}"

# Add sourcery toolchain version to external recipe versions
EXTERNAL_PV_SUFFIX ?= "-${SOURCERY_VERSION}"
EXTERNAL_PV_SUFFIX_allarch = ""

BUILDCFG_VARS += "EXTERNAL_TOOLCHAIN SOURCERY_VERSION GCC_VERSION"

# Adjust tunings to ensure we're using Sourcery G++ multilibs
require conf/distro/include/sourcery-tuning.inc
