OpenEmbedded/Yocto layer for the Sourcery G++ toolchain
=======================================================

Usage & Instructions
--------------------

- Ensure that you have the Sourcery G++ toolchain installed.
- If it's an ia32 toolchain, make sure you did *not* let it modify your PATH,
  and if you did, remove it.

  This is necessary because the ia32 Sourcery G++ toolchain
  shipped non-prefixed binaries (e.g. `gcc` rather than `i586-none-linux-gcc`), which
  means bitbake would be unable to run the host's gcc directly anymore.
- Add the meta-sourcery layer to your `BBLAYERS` in `conf/bblayers.conf`. Please make
  certain that it is listed before the `meta` layer, as this ensures meta-sourcery gets
  priority over meta.
- Set `EXTERNAL_TOOLCHAIN = "/path/to/your/sourcery-g++-install"` in `conf/local.conf`.

Optional Functionality
----------------------

- If the user chooses to, they may optionally decide to rebuild the Sourcery G++ glibc
  from source, if they have downloaded the corresponding source archive from Mentor
  Graphics. To so, set `TCMODE = "external-sourcery-rebuild-libc"`, rather than relying
  on the default value of `external-sourcery`. After setting TCMODE appropriately, you
  must also set `CSL_SRC_FILE = "/path/to/your/sourcery-g++-source-tarball"`.

Description of Behavior
-----------------------

The meta-sourcery layer.conf automatically defines `TCMODE` for us, so this is no longer
necessary.  The tcmode performs a number of operations:

- Sets `TARGET_PREFIX` appropriately, after determining what prefix is in use by the toolchain
- Sanity checks `EXTERNAL_TOOLCHAIN`: does the path exist? does the expected sysroot exist?
- Sanity checks execution of the toolchain binaries
- Sets preferences so that the `external-sourcery-toolchain` recipe is used in preference
  to rebuilding various things from source with their own recipes
- Extracts version information from the toolchain (e.g. by running `${TARGET_PREFIX}gcc -v`),
  for use in the `external-sourcery-toolchain` recipe and its binary packages
- Symlinks the toolchain binaries into the toolchain portion of the sysroot. This is done
  in preference to adding the toolchain path to the `PATH`, to avoid the aforementioned
  ia32 issue, and to let us work around certain issues (For example, we create an `ld.bfd`
  link which the kernel build expects, but isn't shipped with the toolchain)
- Adds the external toolchain `PATH` to the setup script emitted when building SDKs (e.g.
  when bitbaking meta-toolchain)
- Sets `GCCVERSION` to the gcc version of the toolchain, to prefer a matching gcc version for
  the target package, if possible. Certain versions of gcc have trouble being built by other
  versions of gcc, so this can avoid such issues.

Contributing
------------

URL: https://github.com/MentorEmbedded/meta-sourcery

To contribute to this layer, please fork and submit pull requests to the above
repository with github, or open issues for any bugs you find, or feature
requests you have.

Content review
--------------

- Fix `GNU_HASH` warnings / obey `LDFLAGS`

    - imx-lib
    - blktrace
    - hostap
    - gdbm
    - setserial
    - irda-utils
    - python
    - perl
