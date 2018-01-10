EXTERNAL_TOOLCHAIN_ABS_PATH ?= "${EXTERNAL_TOOLCHAIN}/bin/"
EXTERNAL_TOOLCHAIN_ABS_PATH_class-cross-canadian ?= ""
EXTERNAL_CROSS_NOPSEUDO_class-cross-canadian ?= "${EXTERNAL_CROSS_BINARIES}"
wrap_bin () {
    bin="$1"
    shift
    script="${D}${bindir}/${TARGET_PREFIX}$bin"
    printf '#!/bin/sh\n' >$script
    for arg in "$@"; do
        printf '%s\n' "$arg"
    done >>"$script"
    printf 'exec ${EXTERNAL_TOOLCHAIN_ABS_PATH}${EXTERNAL_TARGET_SYS}-%s "$@"\n' "$bin" >>"$script"
    chmod +x "$script"
}

do_install () {
    install -d ${D}${bindir}
    for bin in ${EXTERNAL_CROSS_BINARIES}; do
        if [ ! -e "${EXTERNAL_TOOLCHAIN}/bin/${EXTERNAL_TARGET_SYS}-$bin" ]; then
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

