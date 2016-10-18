- Adjust the hardcoded licenses based on version, for now. Better would be if
  the external toolchain provided license data.
- Fix extraction of patterns like `${libdir}/locale/*/*/libc.mo` (glibc) and
  `${libdir}/gcc/*/*/include/omp.h` (libgomp).

- Improvements above and beyond the existing previous features of the layer

    - Attempt to determine the available locales for locale generation/packaging
      dynamically based on what's available in the sysroot.
    - Don't use cp -a (we don't want the permissions/ownership from the
      external toolchain to leak onto our target)
    - Think about using cpio instead of cp
    - Think about hard linking if possible instead of copying, as long as
      do_package doesn't modify files in place, rather than
      unlinking/creating.

    - Improve separation between sourcery and general external bits
    - Refactor and enhance to be able to use this sysroot extraction code to
      be able to support a true native MACHINE, bypassing cross-compilation
      entirely.

    - Re-examine oe-core metadata for our extraction recipes to see if anything
      can be reused (e.g. in libgcc, gcc-runtime)

    - Re-review the Wind River toolchain layers for useful bits
    - Add hooks to be able to handle multilib configurations stored in cpio
      archives rather than directly on disk. This will include:

        - a hook for the search process so we can examine the contents of the
          archives instead of on-disk
        - a hook for the copy process so we can extract instead of copying

    - Add minimum gcc version requirement (>=4.3) due to requirement for
      -print-sysroot/--sysroot=.
    - Test minimum gcc/glibc versions to actually complete a build.

    - Consider reworking external_toolchain_do_install in shell. This would need
      performance testing.
    - Consider reverting the split out of linux-libc-headers-external, as we don't
      want to encourage folks to provide their own -- there are better mechanisms.

- Bugs

    - 2013.11: problems with cross-localedef for bo_CN, et_VE, ar_SD, az_AZ, bo_IN
