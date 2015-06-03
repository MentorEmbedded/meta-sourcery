TOOLCHAIN_HOST_TASK += "${@bb.utils.contains('BBFILE_COLLECTIONS', 'qt5-mel', 'glibc-multilib-link', '', d)}"
