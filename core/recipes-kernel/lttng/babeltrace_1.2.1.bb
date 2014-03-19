SUMMARY = "Babeltrace - Trace Format Babel Tower"
DESCRIPTION = "Babeltrace provides trace read and write libraries in host side, as well as a trace converter, which used to convert LTTng 2.0 traces into human-readable log."
HOMEPAGE = "http://www.efficios.com/babeltrace/"
BUGTRACKER = "https://bugs.lttng.org/projects/babeltrace"

LICENSE = "MIT & GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=76ba15dd76a248e1dd526bca0e2125fa"

inherit autotools

DEPENDS = "glib-2.0 util-linux popt"

SRCREV = "7efc443724d404bc06df5d484be36f7b7d2bcf60"
PV = "1.2.1+git${SRCPV}"

SRC_URI = "git://git.efficios.com/babeltrace.git;branch=stable-1.2 \
           file://lttng-live-missing-header.patch"

SRC_URI_append_arm = " file://do-not-optimize-parser.patch"

S = "${WORKDIR}/git"

do_configure_prepend () {
    ( cd ${S}; ${S}/bootstrap )
}
