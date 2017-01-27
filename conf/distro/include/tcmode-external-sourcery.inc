#
# Configuration to use external Sourcery G++ toolchain
#

EXTERNAL_TOOLCHAIN ?= "UNDEFINED"

# If the user hasn't set EXTERNAL_TOOLCHAIN, but CODEBENCH_PATH is set, then
# we automatically set both EXTERNAL_TOOLCHAIN and EXTERNAL_TARGET_SYS based
# on the installed toolchains in CODEBENCH_PATH/../toolchains.
CODEBENCH_PATH ?= ""
CODEBENCH_TOOLCHAINS_PATH ?= "${CODEBENCH_PATH}/../toolchains"

# When using a 32-bit external toolchain, we need 32 bit pseudo
HAS_64BIT_TOOLCHAIN = "${@'1' if '${BUILD_ARCH}' == 'x86_64' and os.path.exists('${EXTERNAL_TOOLCHAIN}/${BUILD_SYS}-gnu') else '0'}"
NO32LIBS ?= "${HAS_64BIT_TOOLCHAIN}"

# Align paths with the external toolchain
BASELIB_aarch64 = "lib64"
BASELIB_x86-64 = "lib64"
BASELIB_x86-64_linux-gnux32 = "libx32"

# Ensure that we only attempt to package up locales which are available in the
# external toolchain. In the future, we should examine the external toolchain
# sysroot and determine this accurately.
GLIBC_GENERATE_LOCALES_remove_pn-glibc-locale = "en_US.UTF-8"

# Align localedir with the corresponding path in external toolchain
localedir = "${exec_prefix}/lib/locale"

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
TOOLCHAIN_TARGET_TASK_append = " sdk-env-external-toolchain"

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
EXTERNAL_TARGET_SYSTEMS[powerpc] ?= "powerpc-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[powerpc64] ?= "powerpc-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[arm] ?= "arm-none-linux-gnueabi"
EXTERNAL_TARGET_SYSTEMS[aarch64] ?= "aarch64-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mips] ?= "mips-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mipsel] ?= "mips-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[mips64] ?= "mips64-nlm-linux-gnu mips-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[x86_64] ?= "x86_64-linux-gnu x86_64-amd-linux-gnu i686-pc-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[i686] ?= "i686-pc-linux-gnu"
EXTERNAL_TARGET_SYSTEMS[i586] ?= "i686-pc-linux-gnu"
EXTERNAL_TARGET_SYSTEMS = "${@d.getVarFlag('EXTERNAL_TARGET_SYSTEMS', '${TARGET_ARCH}', True) or '${TARGET_SYS}'}"

def external_target_sys(d):
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)

    systems = d.getVar('EXTERNAL_TARGET_SYSTEMS', True).split()
    target = d.getVar('TARGET_SYS', True)
    if target not in systems:
        systems.append(target)

    for triplet in systems:
        gcc = os.path.join(toolchain_path, 'bin', triplet + '-gcc')
        if os.path.exists(gcc):
            return triplet

    return 'UNKNOWN'

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
LDEMULATION_x86-64 = "elf_${TARGET_ARCH}"
LDEMULATION_x86-64_linux-gnux32 = "elf32_${TARGET_ARCH}"
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

    # Remove already-added toolchain install paths from the PATH, as they can
    # break the build (in particular, the ia32 toolchain, as it provdes
    # non-prefixed binaries).
    path = d.getVar('PATH', False).split(':')
    path = filter(lambda p: not p.endswith('/codebench/bin'), path)
    d.setVar('PATH', ':'.join(path))

    l = d.createCopy()
    l.finalize()

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
    testenv = os.environ.copy()
    for var in ['MGLS_LICENSE_FILE', 'LM_LICENSE_FILE']:
        if var in d:
           testenv[var] = d.getVar(var, True)

    codebench_check(d)

    with tempfile.NamedTemporaryFile(suffix='.c') as f:
        try:
            subprocess.check_output([d.expand('${EXTERNAL_TOOLCHAIN}/bin/${EXTERNAL_TARGET_SYS}-gcc'), '-msgxx-glibc', '-E', f.name], stderr=subprocess.STDOUT, env=testenv, cwd=d.getVar('TOPDIR', True))
        except (OSError, subprocess.CalledProcessError):
            pass
        else:
            d.appendVar('TUNE_CCARGS', ' -msgxx-glibc')
}
toolchain_metadata_setup[eventmask] = "bb.event.ConfigParsed"
addhandler toolchain_metadata_setup

def codebench_check(d):
    codebench_path = d.getVar('CODEBENCH_PATH', True)
    exttc = d.getVar('EXTERNAL_TOOLCHAIN', True)
    if exttc and exttc != 'UNDEFINED':
        if codebench_path:
            bb.warn('Both EXTERNAL_TOOLCHAIN and CODEBENCH_PATH are set. Ignoring CODEBENCH_PATH in preference to EXTERNAL_TOOLCHAIN')
            return
        else:
            auto_codebench_path_fixup(exttc, d)
            codebench_path = d.getVar('CODEBENCH_PATH', True)

    if codebench_path:
        set_vars_from_toolchains(codebench_path, d)

def auto_codebench_path_fixup(exttc, d):
    """Fixups for common issues with EXTERNAL_TOOLCHAIN with CodeBench."""
    if os.path.exists(os.path.join(exttc, 'codebench')):
        newtc = os.path.join(exttc, 'codebench')
        if not os.path.exists(os.path.join(newtc, '..', 'toolchains')):
            bb.warn('EXTERNAL_TOOLCHAIN was set to the root of a codebench install, not the toolchain path')
            bb.warn('Adjusted EXTERNAL_TOOLCHAIN from `{}` to `{}`'.format(exttc, newtc))
        exttc = newtc
        d.setVar('EXTERNAL_TOOLCHAIN', exttc)

    if os.path.exists(os.path.join(exttc, '..', 'toolchains')):
        bb.warn('Detected CodeBench installation, but CODEBENCH_PATH is not set')
        bb.warn('Adjusted CODEBENCH_PATH to `{}` and removed EXTERNAL_TOOLCHAIN'.format(exttc))
        d.setVar('CODEBENCH_PATH', exttc)
        d.setVar('EXTERNAL_TOOLCHAIN', '')

def set_vars_from_toolchains(codebench_path, d):
    toolchains_path = d.getVar('CODEBENCH_TOOLCHAINS_PATH', True)
    if not os.path.exists(toolchains_path):
        if len(os.listdir(os.path.join(codebench_path, 'bin'))) > 1:
            bb.warn('CODEBENCH_PATH is set, but the expected toolchains path ({}) does not exist. Defaulting EXTERNAL_TOOLCHAIN to CODEBENCH_PATH, assuming an old codebench version.'.format(toolchains_path))
        else:
            bb.fatal('Expected toolchains path `{}` does not exist, please ensure that CODEBENCH_PATH is set to a valid CodeBench installation'.format(toolchains_path))

    required_version = d.getVar('SOURCERY_VERSION_REQUIRED', True)
    if required_version:
        required_version = required_version.split('-', 1)[0]

    subdirs = os.listdir(toolchains_path)
    triplets, toolchain_subdir = [], None
    for triplet in d.getVar('EXTERNAL_TARGET_SYSTEMS', True).split():
        if required_version:
            expected_subdir = triplet + '.' + required_version
            if expected_subdir in subdirs:
                triplets.append(triplet)
                toolchain_subdir = expected_subdir
        else:
            for subdir in subdirs:
                if subdir.startswith(triplet + '.'):
                    triplets.append(triplet)
                    toolchain_subdir = subdir

        if triplets:
            break

    if len(triplets) > 1:
        bb.fatal('Error: unable to determine which toolchain to use, as multiple are available ({}). Please set EXTERNAL_TOOLCHAIN manually to the appropriate path in `{}`'.format(', '.join(triplets), toolchains_path))
    elif not triplets:
        bb.fatal('Unable to locate appropriate toolchain in `{}`, please set EXTERNAL_TOOLCHAIN to the correct toolchain path, or install the required CodeBench version'.format(toolchains_path))
    else:
        d.setVar('EXTERNAL_TARGET_SYS', triplets[0])
        d.setVar('EXTERNAL_TOOLCHAIN', os.path.join(toolchains_path, toolchain_subdir))


python toolchain_sanity_check () {
    import shlex
    import subprocess
    import tempfile

    external_toolchain = d.getVar('EXTERNAL_TOOLCHAIN', True)
    if not external_toolchain or external_toolchain == 'UNDEFINED':
        bb.fatal("EXTERNAL_TOOLCHAIN must be set to the path to your sourcery toolchain")

    if not os.path.exists(external_toolchain):
        bb.fatal("EXTERNAL_TOOLCHAIN is invalid: path '%s' does not exist" % external_toolchain)

    bindir = os.path.join(external_toolchain, 'bin')
    if not os.path.exists(bindir):
        bb.fatal("EXTERNAL_TOOLCHAIN is invalid: path '%s' does not exist" % bindir)

    if d.getVar('EXTERNAL_TARGET_SYS', True) == 'UNKNOWN':
        bb.fatal('Unable to locate prefixed gcc binary for %s in EXTERNAL_TOOLCHAIN/bin (%s/bin)' % (d.getVar('TARGET_SYS', True), d.getVar('EXTERNAL_TOOLCHAIN', True)))

    if d.getVar('GCC_VERSION', True) == 'UNKNOWN':
        bb.warn("EXTERNAL_TOOLCHAIN gcc version extraction failed, see debug messages for details")

    with tempfile.TemporaryDirectory() as tmpdir:
        with open(os.path.join(tmpdir, 'test.c'), 'w') as f:
            f.write('int main() {}')

        # The external toolchain recipes haven't necessarily been built, so we
        # need to drop --sysroot= and --no-sysroot-suffix and use the bits in
        # the external toolchain sysroots for this test
        l = d.createCopy()
        l.setVar('TOOLCHAIN_OPTIONS', '')
        l.setVar('HOST_CC_ARCH_remove', '--no-sysroot-suffix')
        cmd = l.expand('${EXTERNAL_TOOLCHAIN}/bin/${CC} ${CFLAGS} ${LDFLAGS} test.c -o test')
        try:
            subprocess.check_output(shlex.split(cmd), cwd=tmpdir, stderr=subprocess.STDOUT)
        except subprocess.CalledProcessError as exc:
            output = exc.output.decode()
            output_indented = ''.join('    ' + l for l in output.splitlines(keepends=True))
            bb.fatal("Toolchain sanity test failed:\n  Command: %s\n  Output:\n%s" % (cmd, output_indented))
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

# Make it clear that this version is the toolchain, not the CodeBench IDE
SOURCERY_TOOLCHAIN_VERSION = "${SOURCERY_VERSION}"

BUILDCFG_VARS += "EXTERNAL_TOOLCHAIN EXTERNAL_TARGET_SYS SOURCERY_TOOLCHAIN_VERSION GCC_VERSION"

# Adjust tunings to ensure we're using Sourcery G++ multilibs
require conf/distro/include/sourcery-tuning.inc

# Use -Wl,--as-needed
require conf/distro/include/as-needed.inc
