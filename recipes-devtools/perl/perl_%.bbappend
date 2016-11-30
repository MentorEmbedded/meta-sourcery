RDEPENDS_${PN}-ptest_remove = "${@bb.utils.contains("MACHINE_FEATURES", "x86", "", "libssp", d)}"
