OpenEmbedded/Yocto layer for the Sourcery G++ toolchain
=======================================================

Layer Dependencies
------------------

- [openembedded-core](https://github.com/openembedded/openembedded-core), with
  a matching branch (i.e. master of oe-core and master of meta-sourcery)

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
  must also set `SOURCERY_SRC_FILE = "/path/to/your/sourcery-g++-source-tarball"` or
  `SOURCERY_SRC_URI = "http://some.domain/some-path"`.

Description of Behavior
-----------------------

The meta-sourcery layer.conf automatically defines `TCMODE` for us, so this is no longer
necessary.  The tcmode performs a number of operations:

- Sets `TARGET_PREFIX` appropriately, after determining what prefix is in use by the toolchain
- Sanity checks `EXTERNAL_TOOLCHAIN`: does the path exist? does the expected sysroot exist?
- Sets preferences so that external recipes are used in preference to building
  them from source, including cross recipes which link/wrap the toolchain
  cross binaries

Contributing
------------

To contribute to this layer, please fork and submit pull requests to the
github [repository](https://github.com/MentorEmbedded/meta-sourcery), or open
issues for any bugs you find, or feature requests you have.

To Do List
----------

See [TODO.md](TODO.md)
