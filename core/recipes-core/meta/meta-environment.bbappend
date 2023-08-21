# CodeBench needs TARGET_PREFIX to align with the external toolchain
TARGET_PREFIX:tcmode-external-sourcery = "${EXTERNAL_TARGET_SYS}-"

SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchain"
EXTERNAL_TOOLCHAIN_RELBIN = "${@os.path.relpath(d.getVar('EXTERNAL_TOOLCHAIN_BIN'), d.getVar('EXTERNAL_TOOLCHAIN'))}"
TOOLCHAIN_PATH:tcmode-external-sourcery = "${SDKPATHTOOLCHAIN}/${EXTERNAL_TOOLCHAIN_RELBIN}"

create_sdk_files:append:tcmode-external-sourcery () {
    script=${SDK_OUTPUT}/${SDKPATH}/environment-setup-${REAL_MULTIMACH_TARGET_SYS}
    cat >>"$script" <<END
TOOLCHAIN_PATH=${TOOLCHAIN_PATH}
if [ -e "\$TOOLCHAIN_PATH" ]; then
    PATH="\$PATH:\$TOOLCHAIN_PATH"
fi
END
}
