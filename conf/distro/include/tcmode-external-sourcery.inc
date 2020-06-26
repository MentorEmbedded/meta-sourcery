require conf/distro/include/tcmode-external-oe-sdk.inc

TCMODEOVERRIDES .= ":tcmode-external-sourcery"

# This is defined in default-providers.inc, which is parsed before the tcmode,
# so we can't use ?=
PREFERRED_PROVIDER_linux-libc-headers = "linux-libc-headers-external"
PNBLACKLIST[linux-libc-headers] = "not building with an external toolchain"
PNBLACKLIST[linux-libc-headers-yocto] = "not building with an external toolchain"

# Disable pseudo for these cross binaries
EXTERNAL_CROSS_NOPSEUDO = "gcc g++ cpp"

# Use tuning-defined baselib to align with multilib and external toolchain
baselib = "${@d.getVar('BASE_LIB_tune-' + (d.getVar('DEFAULTTUNE') or 'INVALID')) or d.getVar('BASELIB')}"

# This overrides the function from external-oe-sdk to handle multiple scripts
python extract_env_setup_metadata() {
    import pathlib

    external_toolchain = pathlib.Path(d.getVar('EXTERNAL_TOOLCHAIN'))
    if external_toolchain.is_absolute() and external_toolchain.is_dir():
        setup = d.getVar('EXTERNAL_TOOLCHAIN_SETUP_SCRIPT')
        if setup:
            setup = pathlib.Path(setup)
            env = parse_setup_script(setups[0])
        else:
            candidates = select_appropriate_setup_script(d, external_toolchain)
            if not candidates:
                setup = None
            elif len(candidates) > 1:
                bb.fatal("tcmode-external-sourcery: multiple candidate setup scripts found, please specify with EXTERNAL_TOOLCHAIN_SETUP_SCRIPT: {}".format(" ".join(candidates)))
            else:
                setup, env = candidates[0]
                bb.debug(1, "tcmode-external-sourcery: selected setup script {}".format(setup))

        if not setup:
            bb.fatal('tcmode-external-sourcery: failed to determine setup script path for sdk at {}, please set EXTERNAL_TOOLCHAIN_SETUP_SCRIPT to the full path to the environment setup script.'.format(external_toolchain))

        target_sys = env.get('TARGET_PREFIX')[:-1]
        native_sysroot = pathlib.Path(env.get('OECORE_NATIVE_SYSROOT'))

        d.setVar('EXTERNAL_TARGET_SYS', str(target_sys))
        d.setVar('EXTERNAL_TOOLCHAIN_BIN', str(native_sysroot / 'usr' / 'bin' / target_sys))
        d.setVar('EXTERNAL_TOOLCHAIN_SYSROOT', env.get('SDKTARGETSYSROOT'))
}

# These are treated as prefixes to items in PACKAGE_ARCHS at this time.
#
# An armv7-a sysroot will run fine with armv7ve machines, as the latter just
# have virtualization extensions. We need to handle this sort of thing
# directly in this case, as for some reason the armv7-a tunes aren't listed in
# the armv7ve PACKAGE_ARCHS.
EXTERNAL_TOOLCHAIN_ARCH_COMPAT[armv7ve] += "armv7a"

def select_appropriate_setup_script(d, external_toolchain):
    candidates = []

    tune_pkgarch = d.getVar('TUNE_PKGARCH')
    arch_setups = list(external_toolchain.glob('environment-setup-' + tune_pkgarch + '-*'))
    if arch_setups:
        # Exact match
        for setup in arch_setups:
            setup_env = parse_setup_script(setup)
            candidates.append((setup, setup_env))
        return candidates

    target_arch = d.getVar('TARGET_ARCH')
    target_os = d.getVar('TARGET_OS')
    baselib = d.getVar('baselib')
    #tune_ccargs = d.getVar('TUNE_CCARGS')
    package_archs = d.getVar('PACKAGE_ARCHS').split()

    arch_compat = d.getVarFlags('EXTERNAL_TOOLCHAIN_ARCH_COMPAT')
    setups = external_toolchain.glob('environment-setup-*')
    for setup in setups:
        setup_env = parse_setup_script(setup)
        if target_arch != setup_get(setup_env, 'OECORE_TARGET_ARCH'):
            bb.debug(1, "tcmode-external-sourcery: TARGET_ARCH `{}` doesn't match `{}` in `{}`".format(target_arch, setup_get(setup_env, 'OECORE_TARGET_ARCH'), setup))
            continue
        if target_os != setup_get(setup_env, 'OECORE_TARGET_OS'):
            bb.debug(1, "tcmode-external-sourcery: TARGET_OS `{}` doesn't match `{}` in `{}`".format(target_os, setup_get(setup_env, 'OECORE_TARGET_OS'), setup))
            continue
        if baselib != setup_get(setup_env, 'OECORE_BASELIB'):
            bb.debug(1, "tcmode-external-sourcery: BASELIB `{}` doesn't match `{}` in `{}`".format(baselib, setup_get(setup_env, 'OECORE_BASELIB'), setup))
            continue

        setup_tune_pkgarch = setup_env.get('TUNE_PKGARCH')
        if setup_tune_pkgarch:
            if setup_tune_pkgarch not in package_archs:
                compatible = False
                for from_arch, compat_archs in arch_compat.items():
                    for compat_arch in compat_archs.split():
                        adjusted_archs = [a.replace(from_arch, compat_arch) for a in package_archs]
                        if setup_tune_pkgarch in adjusted_archs:
                            compatible = True

                if not compatible:
                    bb.debug(1, "tcmode-external-sourcery: skipping incompatible {}: TUNE_PKGARCH `{}` not found in PACKAGE_ARCHS `{}`".format(setup, setup_tune_pkgarch, package_archs))
                    continue
        candidates.append((setup, setup_env))

    return candidates

def setup_get(setup, field):
    if not field in setup:
        bb.fatal("tcmode-external-sourcery: no variable `{}` found in `{}`".format(field, setup))
    return setup[field]

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
    # Ensure that changes to toolchain licensing don't affect checksums
    license_vars = ['MGLS_LICENSE_FILE', 'LM_LICENSE_FILE']
    d.appendVar('BB_HASHBASE_WHITELIST', ' ' + ' '.join(license_vars))
}
sourcery_metadata_setup[eventmask] = "bb.event.ConfigParsed"
addhandler sourcery_metadata_setup
