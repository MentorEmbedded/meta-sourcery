SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchain"

inherit cross-canadian

LICENSE_FILE = "${EXTERNAL_TOOLCHAIN}/legal/ReadMe_OSS.html"
# The ReadMe is included as a source file, not directly copied from
# EXTERNAL_TOOLCHAIN in do_populate_lic, to ensure that it is also included in any
# source archival methods, such as archiver.
SRC_URI = "file://${LICENSE_FILE}"
LIC_FILES_CHKSUM = "file://${WORKDIR}/ReadMe_OSS.html"

# This license is simply an identifier to refer to the union of all the licenses of all
# the included components of the external toolchain, as described in the included
# ReadMe_OSS.html file.
LICENSE = "Sourcery-Toolchain"

PN .= "-${TRANSLATED_TARGET_ARCH}"
SKIPPED = "1"
SKIPPED:tcmode-external = "0"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"

python () {
    external = d.getVar("EXTERNAL_TOOLCHAIN")
    if not external or not os.path.isdir(external) or d.getVar("SKIPPED") == "1":
        raise bb.parse.SkipRecipe("An existing external toolchain at EXTERNAL_TOOLCHAIN is required and TCMODE must be set appropriately")
}

do_unpack[postfuncs] += "move_readme"

move_readme () {
    if [ -n "${SRC_URI}" ]; then
        mv "${WORKDIR}/${@d.getVar('LICENSE_FILE')[1:]}" "${WORKDIR}/ReadMe_OSS.html"
    fi
}

deltask do_configure
deltask do_compile

do_install () {
    bbnote "Copying ${EXTERNAL_TOOLCHAIN}/. to ${D}/${SDKPATHTOOLCHAIN}/"
    install -d ${D}/$(dirname ${SDKPATHTOOLCHAIN})
    cp -a "${EXTERNAL_TOOLCHAIN}/." "${D}/${SDKPATHTOOLCHAIN}/"
}

do_package_qa[noexec] = "1"

FILES:${PN} += "${SDKPATHTOOLCHAIN}"
