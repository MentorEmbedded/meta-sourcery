inherit external-toolchain cross

EXTERNAL_CROSS_BINARIES ?= ""
EXTERNAL_CROSS_NOPSEUDO = "gcc g++ cpp"

wrap_bin () {
    bin="$1"
    shift
    script="${D}${bindir}/${TARGET_PREFIX}$bin"
    printf '#!/bin/sh\n' >$script
    for arg in "$@"; do
        printf '%s\n' "$arg"
    done >>"$script"
    printf 'exec ${EXTERNAL_TOOLCHAIN}/bin/${TARGET_PREFIX}%s "$@"\n' "$bin" >>"$script"
    chmod +x "$script"
}

do_install () {
    install -d ${D}${bindir}
    for bin in ${EXTERNAL_CROSS_BINARIES}; do
        if [ ! -e "${EXTERNAL_TOOLCHAIN}/bin/${TARGET_PREFIX}$bin" ]; then
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
