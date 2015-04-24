# Work around the fact that gcc/g++ is not run under pseudo at the moment to
# bypass a different bug. Ncurses links the libs directly into place in the
# destination, so we need to correct the ownership here.
do_install_append () {
    chown root:root ${D}${base_libdir}/lib*.so.* ${D}${libdir}/lib*.so.*
}
