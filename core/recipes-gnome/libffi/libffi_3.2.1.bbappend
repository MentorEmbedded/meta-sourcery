SRC_URI_append_tcmode-external-sourcery = " file://o32_eh_frame_fix.patch \
                                            file://n32_eh_frame_fix.patch"
FILESEXTRAPATHS_prepend := "${THISDIR}:"
