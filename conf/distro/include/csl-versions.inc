def csl_run(d, cmd, *args):
	import bb.process
	import subprocess

	topdir = d.getVar('TOPDIR', True)
	toolchain_path = d.getVar('EXTERNAL_TOOLCHAIN', True)
	if not toolchain_path:
		return 'UNKNOWN', 'UNKNOWN'

	target_prefix = d.getVar('TARGET_PREFIX', True)
	path = os.path.join(toolchain_path, 'bin', target_prefix + cmd)
	args = [path] + list(args)

	return bb.process.run(args, cwd=topdir, stderr=subprocess.PIPE)

def csl_get_version(d):
        try:
		stdout, stderr = csl_run(d, 'gcc', '-v')
	except bb.process.CmdError as exc:
		bb.error('Failed to obtain CodeSourcery toolchain version: %s' % exc)
		return 'UNKNOWN'
	else:
		last_line = stderr.splitlines()[-1]
		return last_line

def csl_get_main_version(d):
	version = csl_get_version(d)
	if version != 'UNKNOWN':
		return version.split()[-1].rstrip(')')
	else:
		return version

def csl_get_gcc_version(d):
	version = csl_get_version(d)
	if version != 'UNKNOWN':
		return version.split()[2]
	else:
		return version

def csl_get_libc_version(d):
	sysroot = d.getVar('EXTERNAL_TOOLCHAIN_SYSROOT', True)
	if not sysroot:
	        return 'UNKNOWN'

	libpath = os.path.join(sysroot, 'lib')
	if os.path.exists(libpath):
		for file in os.listdir(libpath):
			if file.find('libc-') == 0:
				return file[5:-3]
	return 'UNKNOWN'

def csl_get_kernel_version(d):
	sysroot = d.getVar('EXTERNAL_TOOLCHAIN_SYSROOT', True)
	if not sysroot:
	        return 'UNKNOWN'

	vf = os.path.join(sysroot, 'usr/include/linux/version.h')

	try:
		f = open(vf, 'r')
	except (OSError, IOError):
		return 'UNKNOWN'

        with f:
	    for line in f.readlines():
		if 'LINUX_VERSION_CODE' in line:
			ver = int(line.split()[2])
			maj = ver / 65536
			ver = ver % 65536
			min = ver / 256
			ver = ver % 256
			return '%s.%s.%s' % (maj, min, ver)
	return 'UNKNOWN'

def csl_get_gdb_version(d):
	try:
		stdout, stderr = csl_run(d, 'gdb', '-v')
	except bb.process.CmdError as c:
		return 'UNKNOWN(%s)' % c
	else:
		first_line = stdout.splitlines()[0]
		return first_line.split()[-1]

python csl_version_handler () {
    if not isinstance(e, bb.event.ConfigParsed):
        return
    d = e.data
    ld = d.createCopy()
    ld.finalize()

    d.setVar('CSL_VER_MAIN', csl_get_main_version(ld))
    d.setVar('CSL_VER_GCC', csl_get_gcc_version(ld))
    d.setVar('CSL_VER_LIBC', csl_get_libc_version(ld))
    d.setVar('CSL_VER_KERNEL', csl_get_kernel_version(ld))
    d.setVar('CSL_VER_GDB', csl_get_gdb_version(ld))
}
addhandler csl_version_handler

# Ensure that any variable which includes the --sysroot (CC, CXX, etc) also
# depends on the toolchain version
TOOLCHAIN_OPTIONS[vardeps] += "CSL_VER_MAIN CSL_VER_GCC"
