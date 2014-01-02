#
# Configuration to use external Sourcery G++ toolchain
#

EXTERNAL_TOOLCHAIN ?= "UNDEFINED"

# Ensure that pseudo builds both 32 bit and 64 bit binaries, as we need the
# 32 bit pseudo to execute the 32 bit external toolchain binaries without
# errors or warnings.
NO32LIBS ?= "0"

# Don't ship any toolchain binaries in the sdk for the time being
# FIXME: find a way to do this just for the recipes which include those
# binaries. Potentially we could replace the packagegroup with an alternative
# version which includes nothing, or provide a bbclass which filters it out at
# RecipeParsed time.
#TOOLCHAIN_HOST_TASK ?= "nativesdk-packagegroup-sdk-host meta-environment-${TRANSLATED_TARGET_ARCH}"

# Add the external toolchain to the sdk setup script PATH
INHERIT += "toolchain-scripts-external"

localedir = "${exec_prefix}/lib/locale"

CSL_TARGET_SYS_powerpc ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
CSL_TARGET_SYS_powerpc64 ?= "powerpc-linux-gnu powerpc-mentor-linux-gnu"
CSL_TARGET_SYS_arm ?= "arm-none-linux-gnueabi arm-mentor-linux-gnueabi"
CSL_TARGET_SYS_mips ?= "mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_mipsel ?= "mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_mips64 ?= "mips64-nlm-linux-gnu mips-linux-gnu mips-mentor-linux-gnu"
CSL_TARGET_SYS_x86-64 ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
CSL_TARGET_SYS_i686 ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
CSL_TARGET_SYS_i586 ?= "i686-pc-linux-gnu i686-mentor-linux-gnu"
CSL_TARGET_SYS = "${TARGET_SYS}"

# Shared configuration and code for the various sourcery toolchain things.
# XXX -- this should be the sysroot subdir, fix later
CSL_TARGET_CORE ?= "${EXTERNAL_TOOLCHAIN_SYSROOT_MULTILIB}"

def csl_target_sys(d):
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)

    for triplet in d.getVar('CSL_TARGET_SYS', True).split():
        gcc = os.path.join(toolchain_path, 'bin', triplet + '-gcc')
        if os.path.exists(gcc):
            return triplet
    return '${TARGET_SYS}'

# We used to set TARGET_PREFIX, but this breaks multilibs if both
# multilibs use the same CSL_TARGET_SYS value.
TOOLCHAIN_BINARY_TRIPLET = "${@csl_target_sys(d)}"
TOOLCHAIN_BINARY_PREFIX = "${TOOLCHAIN_BINARY_TRIPLET}-"

PREFERRED_PROVIDER_linux-libc-headers = "sourcery-tc-prebuilt"
PREFERRED_PROVIDER_linux-libc-headers-dev = "sourcery-tc-prebuilt"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-initial = "sourcery-tc-prebuilt"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc = "sourcery-tc-cross"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-initial = "sourcery-tc-cross"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-intermediate = "sourcery-tc-cross"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}g++ = "sourcery-tc-cross"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}binutils = "sourcery-tc-cross"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}compilerlibs = "sourcery-tc-cross"
PREFERRED_PROVIDER_libgcc = "sourcery-tc-prebuilt"
PREFERRED_PROVIDER_gdbserver = "sourcery-tc-prebuilt"
PREFERRED_PROVIDER_binutils-cross-canadian-${TRANSLATED_TARGET_ARCH} = "sourcery-tc-cross-canadian-${TRANSLATED_TARGET_ARCH}"
PREFERRED_PROVIDER_gcc-cross-canadian-${TRANSLATED_TARGET_ARCH} = "sourcery-tc-cross-canadian-${TRANSLATED_TARGET_ARCH}"
PREFERRED_PROVIDER_gdb-cross-canadian-${TRANSLATED_TARGET_ARCH} = "sourcery-tc-cross-canadian-${TRANSLATED_TARGET_ARCH}"

# The tcmode-external-sourcery-rebuild-libc file would override these
# with eglibc-sourcery, which rebuilds from source.
PREFERRED_PROVIDER_virtual/libc ?= "eglibc-sourcery-binary"
PREFERRED_PROVIDER_virtual/libiconv ?= "eglibc-sourcery-binary"
PREFERRED_PROVIDER_virtual/libintl ?= "eglibc-sourcery-binary"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc ?= "eglibc-sourcery-binary"
PREFERRED_PROVIDER_eglibc ?= "eglibc-sourcery-binary"

# Set the sourcery-tc to be the preferred toolchain provider
# Oprofile requires an actually compiled binutils...
#PREFERRED_PROVIDER_binutils ?= "sourcery-tc"
PREFERRED_PROVIDER_gcc      ?= "sourcery-tc"
PREFERRED_PROVIDER_prelink  ?= "sourcery-tc"

VIRTUAL-RUNTIME_toolchain        = "sourcery-tc toolchain-wrappers-${@(d.getVar("DEFAULTTUNE", True) or "").replace('-', '_')}${TARGET_VENDOR}-${TARGET_OS}"

# Due to the nature of multilibs, we need to manually specify the libgcc dependency so 
# we can be sure to get the x86-64 specific version.
VIRTUAL-RUNTIME_toolchain_x86-64 = "lib32-sourcery-tc toolchain-wrappers-${@(d.getVar("DEFAULTTUNE", True) or "").replace('-', '_')}${TARGET_VENDOR}-${TARGET_OS} libgcc"

# No need to re-compile the locale files
GLIBC_INTERNAL_USE_BINARY_LOCALE = "precompiled"
ENABLE_BINARY_LOCALE_GENERATION = ""

TOOLCHAIN_OPTIONS = " --sysroot=${STAGING_DIR_HOST}"

# Get the "base" sysroot.
def sourcery_get_base_sysroot(d):
    import subprocess
    # When looking at native things, we probably don't have a binary toolchain
    # for them, so we wouldn't be able to check it anyway.
    if bb.data.inherits_class("native", d):
        return ""
    sysroot_cmd = "${EXTERNAL_TOOLCHAIN}/bin/${TOOLCHAIN_BINARY_PREFIX}gcc -print-sysroot"
    try:
        toolchain_sysroot = bb.process.run(bb.data.expand(sysroot_cmd, d),
                                           stderr=subprocess.PIPE,
                                           env={"PATH": d.getVar('PATH', True)})[0].rstrip()
    except bb.process.CmdError as exc:
        bb.warn("Failed with path: %s" % d.getVar('PATH', True))
        bb.warn(str(exc))
        return "INVALID_CONFIG"
    else:
        return toolchain_sysroot

# Get the multilib-specific sysroot.
def sourcery_get_sysroot(d):
    import subprocess
    # When looking at native things, we probably don't have a binary toolchain
    # for them, so we wouldn't be able to check it anyway.
    if bb.data.inherits_class("native", d):
        return ""
    sysroot_cmd = "${EXTERNAL_TOOLCHAIN}/bin/${TOOLCHAIN_BINARY_PREFIX}gcc ${TARGET_CC_ARCH} -print-sysroot"
    try:
        toolchain_sysroot = bb.process.run(bb.data.expand(sysroot_cmd, d),
                                           stderr=subprocess.PIPE,
                                           env={"PATH": d.getVar('PATH', True)})[0].rstrip()
    except bb.process.CmdError as exc:
        bb.warn("Failed with path: %s" % d.getVar('PATH', True))
        bb.warn(str(exc))
        return "INVALID_CONFIG"
    else:
        return toolchain_sysroot

# Get the multilib subdir.
def sourcery_get_sysroot_multilib(d):
    base = d.getVar("EXTERNAL_TOOLCHAIN_BASE_SYSROOT", True)
    sysroot = d.getVar("EXTERNAL_TOOLCHAIN_SYSROOT", True)
    if sysroot.startswith(base):
        remainder = sysroot[len(base):]
        if remainder.startswith("/"):
            return remainder[1:]
        else:
            return ""
    else:
        bb.fatal("Error: Sysroot directory does not start with base sysroot directory?")
        return "UNKNOWN"


EXTERNAL_TOOLCHAIN_SYSROOT = "${@sourcery_get_sysroot(d)}"
EXTERNAL_TOOLCHAIN_BASE_SYSROOT = "${@sourcery_get_base_sysroot(d)}"
EXTERNAL_TOOLCHAIN_SYSROOT_MULTILIB = "${@sourcery_get_sysroot_multilib(d)}"

CSL_IS_PRO = "${@'1' if os.path.exists('${EXTERNAL_TOOLCHAIN}/license') else '0'}"

TUNE_CCARGS_append_x86 = " ${@'-msgxx-glibc' if CSL_IS_PRO == '1' else ''}"
TUNE_CCARGS_append_x86-64 = " ${@'-msgxx-glibc' if CSL_IS_PRO == '1' else ''}"

LDEMULATION = ""
LDEMULATION_ENDIAN = "${@'bt' if 'bigendian' in TUNE_FEATURES.split() else 'lt'}"
LDEMULATION_BITS = "${@'64' if 'n64' in TUNE_FEATURES.split() else '32'}"
LDEMULATION_mips64 = "elf${LDEMULATION_BITS}${LDEMULATION_ENDIAN}smip${@bb.utils.contains('TUNE_FEATURES', 'n32', 'n32', '', d)}"
TUNE_LDARGS += "${@'-m ${LDEMULATION}' if LDEMULATION else ''}"

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

    # Remove already-added toolchain install paths from the PATH, as they can
    # break the build (in particular, the ia32 toolchain, as it provdes
    # non-prefixed binaries).
    install_prefix_default = bb.data.expand('${HOME}/CodeSourcery/', l)
    path = d.getVar('PATH', False).split(':')
    path = filter(lambda p: not p.startswith(install_prefix_default), path)
    d.setVar('PATH', ':'.join(path))

    external_toolchain = l.getVar('EXTERNAL_TOOLCHAIN', True)
    if not external_toolchain or external_toolchain == 'UNDEFINED':
        bb.fatal("Error: EXTERNAL_TOOLCHAIN must be set to the path to your sourcery toolchain")

    if not os.path.exists(external_toolchain):
        bb.fatal("Error: EXTERNAL_TOOLCHAIN path '%s' does not exist" % external_toolchain)

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

require conf/distro/include/csl-versions.inc

def get_gcc_version_prefix(d):
    ver = d.getVar('CSL_VER_GCC', True)
    if ver:
        components = ver.split('.')
        if len(components) > 1:
            return '.'.join(components[:2])
    else:
        return '4.7%'

# Prefer a matching gcc version
GCCVERSION ?= "${@get_gcc_version_prefix(d)}%"

BUILDCFG_VARS += "CSL_VER_MAIN EXTERNAL_TOOLCHAIN"
SDKGCCVERSION ?= "${GCCVERSION}"
PREFERRED_VERSION_gcc-crosssdk ?= "${SDKGCCVERSION}"
PREFERRED_VERSION_gcc-crosssdk-initial ?= "${SDKGCCVERSION}"
PREFERRED_VERSION_gcc-crosssdk-intermediate ?= "${SDKGCCVERSION}"
PREFERRED_VERSION_nativesdk-gcc-runtime ?= "${SDKGCCVERSION}"
PREFERRED_VERSION_nativesdk-libgcc ?= "${SDKGCCVERSION}"

# Shared workspace for a copy of the prebuilt toolchain.
TOOLCHAIN_SYSROOT_COPY = "${TMPDIR}/work-shared/wrl-toolchain-binaries-${DEFAULTTUNE}"
# Some prebuilt layers might specify a different debug path.
EXTERNAL_SOURCERY_DEBUGSRC = "/usr/src/debug"
# The toolchain links should all be in one place, so we can find all
# the sets of toolchain links.
TOOLCHAIN_SHARED_BINDIR ?= "${STAGING_DIR_NATIVE}${bindir_native}/${TOOLCHAIN_BINARY_TRIPLET}"
PATH .= ":${TOOLCHAIN_SHARED_BINDIR}"

# By default, we don't allow you to rebuild from source...
PNBLACKLIST[eglibc-sourcery-compile] = "Use the external-sourcery-build-libc TCMODE_EXTRA to rebuild from source."

require conf/distro/include/external-sourcery-blacklist.inc

TCMODE_EXTRA ?= ""

include conf/distro/include/tcmode-extra-${@["", "build-target-toolchain"]["build-target-toolchain" in d.getVar('TCMODE_EXTRA', True).split()]}.inc
include conf/distro/include/tcmode-extra-${@["", "build-libc"]["build-libc" in d.getVar('TCMODE_EXTRA', True).split()]}.inc
