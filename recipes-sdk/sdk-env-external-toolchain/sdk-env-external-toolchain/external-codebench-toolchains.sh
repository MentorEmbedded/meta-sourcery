if [ -d "$scriptdir/../../../toolchains" ]; then
    for d in "$scriptdir/../../../toolchains/"*/bin; do
        if [ -d "$d" ]; then
            PATH="$PATH:$d"
        fi
    done
fi
