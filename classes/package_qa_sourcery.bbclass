inherit package

SOURCERY_QA = "host-user-contaminated"

# We need to test in fakeroot context to check file ownership
do_package_qa[fakeroot] = "1"

HOST_USER_UID := "${@os.getuid()}"
HOST_USER_UID[type] = "integer"
HOST_USER_GID := "${@os.getgid()}"
HOST_USER_GID[type] = "integer"

QAPATHTEST[host-user-contaminated] = "package_qa_check_host_user"
def package_qa_check_host_user(path, name, d, elf, messages):
    """Check for files outside of /home which are owned by the user running bitbake."""

    if not os.path.lexists(path):
        return

    check_uid = oe.data.typed_value('HOST_USER_UID', d)
    check_gid = oe.data.typed_value('HOST_USER_GID', d)

    dest = d.getVar('PKGDEST', True)
    home = os.path.join(dest, 'home')
    if path == home or path.startswith(home + os.sep):
        return

    stat = os.lstat(path)
    if stat.st_uid == check_uid:
        messages["host-user-contaminated"] = "%s is owned by uid %d, which is the same as the user running bitbake. This may be due to host contamination" % (path, check_uid)
        return False

    if stat.st_gid == check_gid:
        messages["host-user-contaminated"] = "%s is owned by gid %d, which is the same as the user running bitbake. This may be due to host contamination" % (path, check_gid)
        return False
    return True
