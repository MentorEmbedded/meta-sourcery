WRAPPER_PREFIX = "${MULTIMACH_TARGET_SYS}-"
TARGET_RAW_PREFIX = "${CSL_TARGET_SYS}-"
OE_TOOLCHAIN_BINPATH = "/opt/windriver/toolchain/bin"

# Skip wrappers for the following items...
TOOLCHAIN_WRAPPER_DEBUGGER = ""
TOOLCHAIN_WRAPPER_MKLIBS = ""

# Disable the directory poisoning when used on the target
WRAPPER_EXTRA_TUNE_CCARGS = "-Wno-poison-system-directories"
WRAPPER_EXTRA_TUNE_LDARGS = "--no-poison-system-directories"

RPROVIDES_${PN} += " \
	binutils-symlinks \
	cpp-symlinks \
	gcc-symlinks \
	g++-symlinks \
	${BPN}-${@(d.getVar("DEFAULTTUNE", True) or "").replace('-', '_')}${TARGET_VENDOR}-${TARGET_OS} \
	"
RREPLACES_${PN} += " \
	binutils-symlinks \
	cpp-symlinks \
	gcc-symlinks \
	g++-symlinks \
	"
