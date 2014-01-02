require recipes-core/eglibc/eglibc-package.inc

RPROVIDES_${PN} += "${TCLIBC} eglibc"
RPROVIDES_${PN}-dbg += "${TCLIBC}-dbg eglibc-dbg"
RPROVIDES_${PN}-dev += "${TCLIBC}-dev eglibc-dev"
RPROVIDES_${PN}-doc += "${TCLIBC}-doc eglibc-doc"
RPROVIDES_${PN}-extra-nss += "${TCLIBC}-extra-nss eglibc-extra-nss"
RPROVIDES_${PN}-gconv += "${TCLIBC}-gconv eglibc-gconv"
RPROVIDES_${PN}-mtrace += "${TCLIBC}-mtrace eglibc-mtrace"
RPROVIDES_${PN}-pcprofile += "${TCLIBC}-pcprofile eglibc-pcprofile"
RPROVIDES_${PN}-pic += "${TCLIBC}-pic eglibc-pic"
RPROVIDES_${PN}-staticdev += "${TCLIBC}-staticdev eglibc-staticdev"
RPROVIDES_${PN}-thread-db += "${TCLIBC}-thread-db eglibc-thread-db"
RPROVIDES_${PN}-utils += "${TCLIBC}-utils eglibc-utils"

# Following was copied from and adapted from libc-common and 
# it is needed to set various PKG_ and related values correctly.
python populate_packages_prepend () {
    pkgs = d.getVar('PACKAGES', True).split()
    bpn = d.getVar('BPN', True)
    prefix = d.getVar('MLPREFIX', True) or ""
    newprefix = 'eglibc'
    if d.getVar('DEBIAN_NAMES', True):
        newprefix = 'libc6'

    # Set the base package...
    d.setVar('PKG_' + prefix + bpn, prefix + newprefix)
    initial = prefix + bpn + '-'
    initial_eglibc = prefix + 'eglibc' + '-'
    for p in pkgs:
        # And all the subpackages.
        if p.startswith(initial):
            renamed = p.replace(bpn, newprefix, 1)
            d.setVar('PKG_' + p, renamed)
        elif p.startswith(initial_eglibc):
            renamed = p.replace('eglibc', newprefix, 1)
            d.setVar('PKG_' + p, renamed)
    # For backward compatibility with old -dbg package
    d.appendVar('RPROVIDES_' + initial + 'dbg', ' ' + prefix + 'libc-dbg')
    d.appendVar('RCONFLICTS_' + initial + 'dbg', ' ' + prefix + 'libc-dbg')
    d.appendVar('RREPLACES_' + initial + 'dbg', ' ' + prefix + 'libc-dbg')
}

