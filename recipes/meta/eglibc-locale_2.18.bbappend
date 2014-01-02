SYSROOT_PREPROCESS_FUNCS += "eglibc_locale_sysroot_preprocess"
eglibc_locale_sysroot_preprocess() {
    if [ -e ${D}${localedir} -a ! -e ${SYSROOT_DESTDIR}${localedir} ]; then
        sysroot_stage_dir ${D}${localedir} ${SYSROOT_DESTDIR}${localedir}
    fi
}
