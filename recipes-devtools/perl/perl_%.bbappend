# libssp support is only available in toolchain for minnowmax so
# remove it until there is a requirement.
RDEPENDS_${PN}-ptest_remove_tcmode-external-sourcery = "${@bb.utils.contains("MACHINE_FEATURES", "x86", "", "libssp", d)}"
