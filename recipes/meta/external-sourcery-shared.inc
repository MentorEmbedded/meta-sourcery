# Shared configuration and code for the various sourcery toolchain things.
# XXX -- this should be the sysroot subdir, fix later
CSL_TARGET_CORE = "${EXTERNAL_TOOLCHAIN_SYSROOT_MULTILIB}"

# Prebuilt toolchains don't build the way most code does.
INHIBIT_DEFAULT_DEPS = "1"

# License applies to this recipe code, not the toolchain itself
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PV = "${CSL_VER_MAIN}"
PR = "r2"

CSL_VER_MAIN ??= ""

python () {
    if not d.getVar("CSL_VER_MAIN"):
        raise bb.parse.SkipPackage("External CSL toolchain not configured (CSL_VER_MAIN not set).")
}

# The prefix which goes on package names; either "" or ${BBEXTENDVARIANT}-
# (including the hyphen).
EXTERNAL_SOURCERY_TOOLCHAIN_VARIANT = "${@['', (d.getVar("BBEXTENDVARIANT", True) or "") + '-'][(d.getVar("BBEXTENDVARIANT", True) or "") != ""]}"
# There's a fancy bit of setup code that copies/extracts relevant hunks of
# the prebuilts, including multilib-specific binaries. A single target covers
# it so sstate can save us time.
EXTERNAL_SOURCERY_TOOLCHAIN_SETUP = "${EXTERNAL_SOURCERY_TOOLCHAIN_VARIANT}external-sourcery-prebuilt:do_setup_shared"

# We keep needing this function.

# cp -a, only "do what I mean".
#
# Specifically, do NOT try to preserve ownership or ACLs; note that
# ACLs would be implied by "mode", but the default behavior will still
# preserve regular permission bits.
#
# It seems that this can be done with cp, because
#   cp -R dirname/. targetname
# works whether or not targetname already exists.
#
# If $warn_or_fail is set to "true", doesn't error out on
# missing files.
tccp() {
        cp -dR --preserve=timestamps "$@" || ${warn_or_fail:-false}
}

# Fancy (shared) code to set up the symlink from a multilib subdirectory
# back to the sysroot directory.

# Postprocessing, which needs to be in both the base recipe and
# the cross-canadian (used for SDK). Accommodates the multilib-friendly
# toolchain's model that sysroot/<multilib-derived-name> is the "real"
# sysroot by symlinking that back to the sysroot.

SYSROOT_PREPROCESS_FUNCS += "external_toolchain_sysroot_adjust"

external_toolchain_sysroot_adjust() {
        # Pick up any symlinks in the top of the sysroot
        external_toolchain_links ${SYSROOT_DESTDIR}

        if [ "${TUNE_PKGARCH}" = "i586" ]; then
                rm -f ${SYSROOT_DESTDIR}/system32
                ln -s . ${SYSROOT_DESTDIR}/system32
        fi
}

external_toolchain_links() {
        # Can't use the wrapper here in the cross-canadian case;
        # base gcc + $CCARGS should work in both cases.
        dest_sysroot="${CSL_TARGET_CORE}"
        if [ -n "$dest_sysroot" ] ; then
                dirs=$dest_sysroot
                target=
                while [ "${dirs%/*}" != "$dirs" ] ; do
                        dirs=${dirs%/*}
                        target=..${target:+/$target}
                done
                rm -f $1/$dest_sysroot
                ln -s "${target:-.}" "$1/$dest_sysroot"
        fi
        # If the usr/lib directory doesn't exist, the toolchain fails to even
        # try to find crti.o in a completely different directory (usr/lib64)
        install -d $1/usr/lib
}
