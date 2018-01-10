SUMMARY = "gdbserver is a program that allows you to run GDB on a different machine than the one which is running the program being debugged"
HOMEPAGE = "http://www.gnu.org/software/gdb/"
SECTION = "devel"
PN .= "-${TARGET_ARCH}"
PV := "${@oe.external.run(d, 'gdb', '-v').splitlines()[0].split()[-1]}"

inherit external-toolchain-cross-canadian

def get_gdb_license(d):
    output = oe.external.run(d, 'gdb', '-v')
    if output != 'UNKNOWN':
        for line in output.splitlines():
            if line.startswith('License '):
                lic = line.split(':', 1)[0]
                return lic.replace('License ', '')
    else:
        return output

LICENSE := "${@get_gdb_license(d)}"
LICENSE[vardepvalue] = "${LICENSE}"


