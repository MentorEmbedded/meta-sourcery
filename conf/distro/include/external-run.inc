def external_run(d, cmd, *args):
    import subprocess

    topdir = d.getVar('TOPDIR', True)
    toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)
    if toolchain_path:
        target_prefix = d.getVar('EXTERNAL_TARGET_SYS', True) + '-'
        path = os.path.join(toolchain_path, 'bin', target_prefix + cmd)
        args = [path] + list(args)

        try:
            output = oe.path.check_output(args, cwd=topdir, stderr=subprocess.STDOUT)
        except oe.path.CalledProcessError as exc:
            import pipes
            bb.warn("{0} failed: {1}".format(' '.join(pipes.quote(a) for a in args), exc.output))
        except OSError as exc:
            import pipes
            bb.warn("{0} failed: {1}".format(' '.join(pipes.quote(a) for a in args), str(exc)))
        else:
            return output

    return 'UNKNOWN'
