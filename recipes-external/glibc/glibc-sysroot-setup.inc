def sysroot_multilib_suffix(d):
    PATH = d.getVar('PATH', True)
    target_cc_arch = d.getVar('TARGET_CC_ARCH', True)
    options = d.getVar('TOOLCHAIN_OPTIONS', True) or d.expand('--sysroot=${STAGING_DIR_TARGET}')
    sysroot = external_run(d, 'gcc', *(target_cc_arch.split() + options.split() + ['-print-sysroot'])).rstrip()
    staging = d.getVar('STAGING_DIR_HOST', True)
    return os.path.relpath(sysroot, staging)

create_multilib_link () {
	dest="$1"
	multilib_suffix="${@sysroot_multilib_suffix(d)}"
	if [ "$multilib_suffix" != "." ]; then
		rm -rf $dest/$multilib_suffix
		ln -s . $dest/$multilib_suffix
	fi
}

SYSROOT_PREPROCESS_FUNCS += "external_toolchain_sysroot_adjust"
external_toolchain_sysroot_adjust() {
	create_multilib_link ${SYSROOT_DESTDIR}

	# If the usr/lib directory doesn't exist, the toolchain fails to even
	# try to find crti.o in a completely different directory (usr/lib64)
	install -d ${SYSROOT_DESTDIR}/usr/lib
}
