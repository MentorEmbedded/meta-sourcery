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
