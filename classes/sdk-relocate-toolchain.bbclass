SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchain"

toolchain_post_install_command_fragment() {

toolchain_rel_script="$target_sdk_dir/${@os.path.relpath(d.getVar('SDKPATHTOOLCHAIN'), d.getVar('SDKPATH'))}/relocate_sdk.sh"
if [ -e "$toolchain_rel_script" ]; then
    $SUDO_EXEC "$toolchain_rel_script"
fi
}

python () {
    # Handle multilibs in the SDK environment, siteconfig, etc files...
    localdata = bb.data.createCopy(d)

    # make sure we only use the WORKDIR value from 'd', or it can change
    localdata.setVar('WORKDIR', d.getVar('WORKDIR'))

    # make sure we only use the SDKTARGETSYSROOT value from 'd'
    localdata.setVar('SDKTARGETSYSROOT', d.getVar('SDKTARGETSYSROOT'))
    localdata.setVar('libdir', d.getVar('target_libdir', False))

    variants = d.getVar("MULTILIB_VARIANTS") or ""
    for variant in [''] + variants.split():
        if variant:
            # Load overrides from 'd' to avoid having to reset the value...
            overrides = d.getVar("OVERRIDES", False) + ":virtclass-multilib-" + variant
            localdata.setVar("OVERRIDES", overrides)
            localdata.setVar("MLPREFIX", variant + "-")

        d.appendVar('SDK_POST_INSTALL_COMMAND', localdata.getVar('toolchain_post_install_command_fragment'))
}
