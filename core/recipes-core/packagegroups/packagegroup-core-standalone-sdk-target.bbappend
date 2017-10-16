# Remove libatomic and libatomic-dev packages as they are not available in codebench toolchain
# for aarch64 architecture
UNSUPPORTED_AARCH64_LIBS = "libatomic libatomic-dev"

RDEPENDS_packagegroup-core-standalone-sdk-target_remove_aarch64 = "${UNSUPPORTED_AARCH64_LIBS}"
