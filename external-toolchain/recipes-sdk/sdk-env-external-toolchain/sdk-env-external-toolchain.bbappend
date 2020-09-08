do_install_append_tcmode-external-sourcery () {
    install -d "${D}/environment-setup.d"
    cat >"${D}/environment-setup.d/external-codebench-toolchains.sh" <<END
sourceryver="${SOURCERY_VERSION}"
sys="${TUNE_PKGARCH}-oe-${TARGET_OS}"
sysroot="${SDK_ARCH}-oesdk-${SDK_OS}"
toolchainsdir="${SDKPATH}/../../../toolchains"
bindir="\$toolchainsdir/\$sys.\${sourceryver%-*}/sysroots/\$sysroot/usr/bin/\$sys"
if [ -e "\$bindir" ]; then
    PATH="\$PATH:\$bindir"
else
    echo >&2 "Warning: failed to add \$bindir to the path: No such file or directory"
fi
END
}
