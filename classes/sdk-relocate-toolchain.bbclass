SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchain"

SDK_POST_INSTALL_COMMAND:append() {
toolchain_rel_script="$target_sdk_dir/${@os.path.relpath(d.getVar('SDKPATHTOOLCHAIN'), d.getVar('SDKPATH'))}/relocate_sdk.sh"
if [ -e "$toolchain_rel_script" ]; then
    $SUDO_EXEC "$toolchain_rel_script"
fi
}
