toolchain_create_sdk_env_script_append () {
    if [ -n "${TOOLCHAIN_PATH_ADD}" ]; then
        echo 'PATH="${TOOLCHAIN_PATH_ADD}$PATH"' >>$script
    fi
}
toolchain_create_tree_env_script_append () {
    if [ -n "${TOOLCHAIN_PATH_ADD}" ]; then
        echo 'PATH="${TOOLCHAIN_PATH_ADD}$PATH"' >>$script
    fi
}
toolchain_create_sdk_env_script_for_installer_append () {
    if [ -n "${TOOLCHAIN_PATH_ADD}" ]; then
        echo 'PATH="${TOOLCHAIN_PATH_ADD}$PATH"' >>$script
    fi
}
