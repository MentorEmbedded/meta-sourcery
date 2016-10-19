inherit external-common

# This patch is needed, at this time, to fix builds with linux-libc-headers
# from the 4.8 Linux kernel or newer, which is not the case for most external
# toolchains. As the external toolchain is available at parse time, we can
# check the version and only apply it when appropriate.
KERNEL_48_PATCH = "file://ppp-fix-building-with-linux-4.8.patch"
SRC_URI_remove_class-target := "${@'${KERNEL_48_PATCH}' if [int(i) for i in ('${EXTERNAL_LIBC_KERNEL_VERSION}' or '0.0').split('.')] < [4, 8] else ''}"
