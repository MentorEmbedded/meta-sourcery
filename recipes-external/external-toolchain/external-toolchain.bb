SDKPATHTOOLCHAIN ?= "${SDKPATH}/toolchain"

inherit cross-canadian

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

deltask do_configure
deltask do_compile

do_install () {
    bbnote "Copying ${EXTERNAL_TOOLCHAIN}/. to ${D}/${SDKPATHTOOLCHAIN}/"
    install -d ${D}/$(dirname ${SDKPATHTOOLCHAIN})
    cp -a "${EXTERNAL_TOOLCHAIN}/." "${D}/${SDKPATHTOOLCHAIN}/"
}

FILES:${PN} += "${SDKPATHTOOLCHAIN}"
