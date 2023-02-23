require conf/distro/include/tcmode-external-oe-sdk.inc

# Also relocate the included external toolchain
SDK_CLASSES ??= ""
SDK_CLASSES:append = " sdk-relocate-toolchain"
IMAGE_CLASSES:append = " ${SDK_CLASSES}"

def codebench_toolchain_dirname(d):
    external, toolchains = d.getVar('EXTERNAL_TOOLCHAIN'), d.getVar('TOOLCHAINS_PATH')
    if external and toolchains and oe.path.is_path_parent(toolchains, external):
        return os.path.relpath(external, toolchains)

EXTERNAL_TOOLCHAINS_DIR = "${@codebench_toolchain_dirname(d) or d.getVar('EXTERNAL_TARGET_SYS')}"
EXTERNAL_TOOLCHAINS_DIR[vardepvalue] = "${EXTERNAL_TOOLCHAINS_DIR}"

SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchains/${EXTERNAL_TOOLCHAINS_DIR}"

TCMODEOVERRIDES .= ":tcmode-external-sourcery"

# Disable pseudo for these cross binaries
EXTERNAL_CROSS_NOPSEUDO = "gcc g++ cpp"

# Use tuning-defined baselib to align with multilib and external toolchain
baselib = "${@d.getVar('BASE_LIB:tune-' + (d.getVar('DEFAULTTUNE') or 'INVALID')) or d.getVar('BASELIB')}"

# Ensure that the licensing variables are available to the toolchain.
export SALT_EXCLUDE_LICENSES
export SALT_INCLUDE_LICENSES
export SALT_LICENSE_SERVER
export SALT_LICENSE_SOURCE 
export SALT_LOGGING_DIR
export SALT_PKGINFO_FILE

def sourcery_version(d):
    version_output = external_run(d, d.getVar('EXTERNAL_CC') or 'gcc', '-v')

    try:
        version = version_output.splitlines()[-1]
    except IndexError:
        bb.error('Unexpected output for {}: {}'.format(d.getVar('EXTERNAL_TOOLCHAIN_BIN') + '/' + (d.getVar('EXTERNAL_CC') or 'gcc') + ' -v', version_output))
        version = 'UNKNOWN'

    if version != 'UNKNOWN':
        main_version = version.split()[-1].rstrip(')')
        if main_version == 'Preview':
            main_version = version.split()[-3]
        elif main_version != '(GCC':
            return main_version
    return 'UNKNOWN'

SOURCERY_VERSION = "${@sourcery_version(d)}"
SOURCERY_VERSION_allarch = ""
SOURCERY_VERSION[vardepvalue] = "${SOURCERY_VERSION}"

# Add sourcery toolchain version to external recipe versions
EXTERNAL_PV_SUFFIX ?= "${@'-${SOURCERY_VERSION}' if d.getVar('SOURCERY_VERSION') != 'UNKNOWN' else ''}"
EXTERNAL_PV_SUFFIX[vardepvalue] = "${EXTERNAL_PV_SUFFIX}"
EXTERNAL_PV_SUFFIX_allarch = ""

# Make it clear that this version is the toolchain, not the CodeBench IDE
SOURCERY_TOOLCHAIN_VERSION = "${SOURCERY_VERSION}"

BUILDCFG_VARS += "${@'SOURCERY_TOOLCHAIN_VERSION' if d.getVar('SOURCERY_VERSION') != 'UNKNOWN' else ''}"

SOURCERY_LICENSE_NETWORK_TASKS = "\
    do_configure \
    do_compile \
    do_compile_kernelmodules \
    do_install \
    do_bundle_initramfs \
    do_configure_ptest \
    do_compile_ptest \
    do_install_ptest \
"

python sourcery_metadata_setup () {
    # Ensure that changes to toolchain licensing don't affect checksums
    d.appendVar('BB_BASEHASH_IGNORE_VARS', ' SALT_LICENSE_SERVER SALT_EXCLUDE_LICENSES SALT_INCLUDE_LICENSES SALT_LOGGING_DIR SALT_PKGINFO_FILE SALT_LICENSE_SOURCE ')

    # Let the toolchain contact the license server by enabling networking in compile tasks
    if d.getVar('SALT_LICENSE_SERVER'):
        for task in d.getVar('SOURCERY_LICENSE_NETWORK_TASKS').split():
            d.setVarFlag(task, 'network', '1')
}
sourcery_metadata_setup[eventmask] = "bb.event.ConfigParsed"
addhandler sourcery_metadata_setup

python extract_env_setup_metadata:prepend() {
    from pathlib import Path
    external_toolchain = d.getVar('EXTERNAL_TOOLCHAIN')
    if not external_toolchain or external_toolchain == 'UNKNOWN':
        external_toolchains = d.getVar('EXTERNAL_TOOLCHAINS')
        if not external_toolchains or external_toolchains == 'UNKNOWN':
            toolchains_path = d.getVar('TOOLCHAINS_PATH')
            if not toolchains_path:
                codebench_path = d.getVar('CODEBENCH_PATH')
                if codebench_path:
                    bb.warn('CODEBENCH_PATH is deprecated, please define TOOLCHAINS_PATH')
                    toolchains_path = Path(codebench_path).resolve().parent / 'toolchains'
                else:
                    bb.fatal('EXTERNAL_TOOLCHAIN, EXTERNAL_TOOLCHAINS, or TOOLCHAINS_PATH must be set')
            external_toolchains = ' '.join(str(p) for p in Path(toolchains_path).glob('*/'))
            if not external_toolchains:
                bb.fatal('{}/../toolchains does not exist or is empty'.format(toolchains_path))
            d.setVar('EXTERNAL_TOOLCHAINS', external_toolchains)
}
