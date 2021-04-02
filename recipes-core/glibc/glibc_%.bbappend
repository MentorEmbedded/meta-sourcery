# Remove files provided by linux-libc-headers
linux_include_subdirs = "asm asm-generic bits drm linux mtd rdma sound sys video"

do_install_append_tcmode-external-sourcery_class-target () {
    for d in ${linux_include_subdirs}; do
        rm -rf "${D}${includedir}/$d"
    done
}

RDEPENDS_${PN}-dev_append_tcmode-external-sourcery_class-target = " linux-libc-headers-dev"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-Do-not-subtract-thread-pointer-in-AArch64-_dl_tlsdes.patch"

def get_pkgversion(d):
    import re
    version_output = external_run(d, d.getVar('EXTERNAL_CC') or 'gcc', '-v')
    version_line = version_output.splitlines()[-1]
    if version_line != 'UNKNOWN':
        m = re.match('gcc version [0-9a-z.]* \(([^)]*)\)', version_line)
        if m:
            return m.group(1)

SOURCERY_NAME ?= "Sourcery CodeBench"
PKGVERSION = "${@get_pkgversion(d) or '${SOURCERY_NAME} ${SOURCERY_VERSION}'}"

EXTRA_OECONF_append_tcmode-external-sourcery = " --with-pkgversion="${PKGVERSION}""
EXTRA_OECONF_append_tcmode-external-sourcery = " --with-bugurl=${BUG_REPORT_URL}"
