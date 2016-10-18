# This patch is needed, at this time, to fix builds with linux-libc-headers
# from the 4.8 Linux kernel or newer, which is not the case for most external
# toolchains. A separate variable is used to ensure the user can undo it from
# local.conf if they're using a newer toolchain.
KERNEL_48_PATCH_REMOVE ?= "file://ppp-fix-building-with-linux-4.8.patch"
SRC_URI_remove = "${KERNEL_48_PATCH_REMOVE}"
