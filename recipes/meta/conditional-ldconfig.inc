do_install_append() {
        # Don't leave this installed if it's not going to be packaged.
        if [ x"${USE_LDCONFIG}" != x"1" ]; then
                rm -f ${D}${sysconfdir}/ld.so.conf ${D}${base_sbindir}/ldconfig
                rmdir ${D}${sysconfdir} 2>/dev/null || true
                rmdir ${D}${base_sbindir} 2>/dev/null || true
        fi
}
