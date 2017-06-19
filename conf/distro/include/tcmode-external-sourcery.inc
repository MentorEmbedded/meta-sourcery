require conf/distro/include/tcmode-external.inc

# If the user hasn't set EXTERNAL_TOOLCHAIN, but CODEBENCH_PATH is set, then
# we automatically set both EXTERNAL_TOOLCHAIN and EXTERNAL_TARGET_SYS based
# on the installed toolchains in CODEBENCH_PATH/../toolchains.
CODEBENCH_PATH ?= ""
CODEBENCH_TOOLCHAINS_PATH ?= "${CODEBENCH_PATH}/../toolchains"

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

# All we care about for the signatures is the result, not how we got there, so
# we don't want EXTERNAL_TARGET_SYSTEMS included.
EXTERNAL_TARGET_SYS ??= "${@external_target_sys(d)}"
EXTERNAL_TARGET_SYS[vardepvalue] = "${EXTERNAL_TARGET_SYS}"
EXTERNAL_TARGET_SYS[vardepsexclude] += "EXTERNAL_TARGET_SYSTEMS EXTERNAL_TOOLCHAIN"

# When using a 32-bit external toolchain, we need 32 bit pseudo
HAS_64BIT_TOOLCHAIN = "${@'1' if '${BUILD_ARCH}' == 'x86_64' and os.path.exists('${EXTERNAL_TOOLCHAIN}/${BUILD_SYS}-gnu') else '0'}"
NO32LIBS ?= "${HAS_64BIT_TOOLCHAIN}"

# Align paths with the external toolchain
BASELIB_aarch64 = "lib64"
BASELIB_x86-64 = "lib64"
BASELIB_x86-64_linux-gnux32 = "libx32"

localedir = "${exec_prefix}/lib/locale"

# Adjust tunings to ensure we're using Sourcery G++ multilibs
require conf/distro/include/sourcery-tuning.inc

# Ensure that we only attempt to package up locales which are available in the
# external toolchain. In the future, we should examine the external toolchain
# sysroot and determine this accurately.
GLIBC_GENERATE_LOCALES_remove_pn-glibc-locale = "en_US.UTF-8"

# No need to re-compile the locale files
GLIBC_INTERNAL_USE_BINARY_LOCALE ?= "precompiled"
ENABLE_BINARY_LOCALE_GENERATION = ""

CSL_IS_PRO = "${@'1' if os.path.exists('${EXTERNAL_TOOLCHAIN}/license') else '0'}"

# Ensure that the licensing variables are available to the toolchain.
export MGLS_LICENSE_FILE
export LM_LICENSE_FILE

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

BUILDCFG_VARS += "SOURCERY_TOOLCHAIN_VERSION"

python sourcery_metadata_setup () {
    import subprocess
    import tempfile

    # Remove already-added toolchain install paths from the PATH, as they can
    # break the build (in particular, the ia32 toolchain, as it provides
    # non-prefixed binaries).
    path = d.getVar('PATH', False).split(':')
    path = filter(lambda p: not p.endswith('/codebench/bin'), path)
    d.setVar('PATH', ':'.join(path))

    # Deal with CODEBENCH_PATH vs EXTERNAL_TOOLCHAIN
    codebench_check(d)

    # Ensure that changes to toolchain licensing don't affect checksums
    license_vars = ['MGLS_LICENSE_FILE', 'LM_LICENSE_FILE']
    d.appendVar('BB_HASHBASE_WHITELIST', ' ' + ' '.join(license_vars))

    # Determine if '-msgxx-glibc' is a valid toolchain option.
    # If so then we need to use it to ensure that the libraries included with
    # the toolchain are used rather than the build host native libraries.
    testenv = os.environ.copy()
    for var in license_vars:
        if var in d:
           testenv[var] = d.getVar(var, True)

    with tempfile.NamedTemporaryFile(suffix='.c') as f:
        try:
            subprocess.check_output([d.expand('${EXTERNAL_TOOLCHAIN}/bin/${EXTERNAL_TARGET_SYS}-gcc'), '-msgxx-glibc', '-E', f.name], stderr=subprocess.STDOUT, env=testenv, cwd=d.getVar('TOPDIR', True))
        except (OSError, subprocess.CalledProcessError):
            pass
        else:
            d.appendVar('TUNE_CCARGS', ' -msgxx-glibc')
}
sourcery_metadata_setup[eventmask] = "bb.event.ConfigParsed"
addhandler sourcery_metadata_setup

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
