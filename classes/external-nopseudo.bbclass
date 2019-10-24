EXTERNAL_CROSS_NOPSEUDO ?= ""

do_install_tcmode-external-sourcery () {
    # Identical to do_install in external-toolchain-cross.bbclass, altered to
    # support EXTERNAL_CROSS_NOPSEUDO
    install -d ${D}${bindir}
    for bin in ${EXTERNAL_CROSS_BINARIES}; do
        if [ ! -e "${EXTERNAL_TOOLCHAIN_BIN}/${EXTERNAL_TARGET_SYS}-$bin" ]; then
            continue
        fi

        disable=0
        for nopseudo in ${EXTERNAL_CROSS_NOPSEUDO}; do
            case "$bin" in
                *$nopseudo)
                    disable=1
                    ;;
            esac
        done
        if [ $disable -eq 1 ]; then
            wrap_bin "$bin" "export PSEUDO_UNLOAD=1"
        else
            wrap_bin "$bin"
        fi
    done
}
