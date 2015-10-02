SYSROOT_PREPROCESS_FUNCS += "external_toolchain_sysroot_adjust"
external_toolchain_sysroot_adjust() {
	# If the usr/lib directory doesn't exist, the toolchain fails to even
	# try to find crti.o in a completely different directory (usr/lib64)
	install -d ${SYSROOT_DESTDIR}/usr/lib
}
