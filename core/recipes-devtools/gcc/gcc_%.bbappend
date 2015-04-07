# unwind.h will come from libgcc-external, we don't want to try to pull it
# from the cross area of the sysroot
python () {
    if d.getVar('TCMODE', True).startswith('external-sourcery'):
        inst = d.getVar('do_install', False).splitlines()
        inst = filter(lambda l: not ('unwind.h' in l and '${STAGING_LIBDIR_NATIVE}' in l), inst)
        d.setVar('do_install', '\n'.join(inst))
}
