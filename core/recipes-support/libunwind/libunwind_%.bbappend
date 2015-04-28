# For now, Sourcery G++ doesn't seem to have the gcc version which breaks the
# libunwind build, and it doesn't ship gold, we don't want to try to use it.
LDFLAGS_remove = "-fuse-ld=gold"
