def glibc_pkgversion (d):
    src = d.getVar('S')
    if src == None:
        return 'INVALID'

    # Exclude some files from calculation of hash:
    # '.git' leads to unpredictable results,
    # '.pc' and 'patches' do not contain sources,
    # 'elf/ldd.bash.in' is modified in do_configure_prepend.
    skip = ['.git', '.pc', 'patches', os.path.join('elf', 'ldd.bash.in')]

    def parse_dir(root, d, h):
        ls = sorted(os.listdir(d)) # sort directory entries to ensure reproducibility
        for f in ls:
            p = os.path.join(d, f) # absolute path
            r = os.path.relpath(p, root) # relative path
            if r in skip:
                return
            if os.path.islink(p):
                pass # skip symbolic links
            elif os.path.isdir(p):
                parse_dir(root, p, h) # recursively run this sub-routine
            elif os.path.isfile(p):
                # Hash all file contents
                with open(p, 'rb') as fd:
                    h.update((r + ' ').encode('utf-8')) # add relative file path
                    h.update(fd.read()) # add file contents
                    h.update(b'!@#$%^&*') # add file separator

    if os.path.isdir(src):
        import hashlib

        # SHA256 should be a good choice
        h = hashlib.sha256()

        # Concatenate all files
        parse_dir(src, src, h)

        # Calculate hash
        return h.hexdigest()

EXTRA_OECONF_append = " --with-pkgversion='built with hash ${@glibc_pkgversion(d)}'"
