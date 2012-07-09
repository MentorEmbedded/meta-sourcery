require recipes-core/eglibc/eglibc-package.inc

PKG_${PN} = "eglibc"
PKG_${PN}-dbg = "eglibc-dbg"
PKG_${PN}-dev = "eglibc-dev"
PKG_${PN}-doc = "eglibc-doc"
PKG_${PN}-extra-nss = "eglibc-extra-nss"
PKG_${PN}-gconv = "eglibc-gconv"
PKG_${PN}-mtrace = "eglibc-mtrace"
PKG_${PN}-pcprofile = "eglibc-pcprofile"
PKG_${PN}-pic = "eglibc-pic"
PKG_${PN}-staticdev = "eglibc-staticdev"
PKG_${PN}-thread-db = "eglibc-thread-db"
PKG_${PN}-utils = "eglibc-utils"

RPROVIDES_${PN} += "${TCLIBC} eglibc"
RPROVIDES_${PN}-dbg += "${TCLIBC}-dbg eglibc-dbg"
RPROVIDES_${PN}-dev += "${TCLIBC}-dev eglibc-dev"
