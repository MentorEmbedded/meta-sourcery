# If the user hasn't set EXTERNAL_TOOLCHAIN, but CODEBENCH_PATH is set, then
# we automatically set both EXTERNAL_TOOLCHAIN and EXTERNAL_TARGET_SYS based
# on the installed toolchains in CODEBENCH_PATH/../toolchains.
CODEBENCH_PATH ?= ""
CODEBENCH_TOOLCHAINS_PATH ?= "${CODEBENCH_PATH}/../toolchains"

python codebench_check () {
    codebench_path = d.getVar('CODEBENCH_PATH', True)
    if not codebench_path:
        return
    elif d.getVar('EXTERNAL_TOOLCHAIN', True):
        bb.warn('Both EXTERNAL_TOOLCHAIN and CODEBENCH_PATH are set. Ignoring CODEBENCH_PATH in preference to EXTERNAL_TOOLCHAIN')
        return

    toolchains_path = d.getVar('CODEBENCH_TOOLCHAINS_PATH', True)
    if not os.path.exists(toolchains_path):
        if len(os.listdir(os.path.join(codebench_path, 'bin'))) > 1:
            bb.warn('CODEBENCH_PATH is set, but the expected toolchains path ({}) does not exist. Defaulting EXTERNAL_TOOLCHAIN to CODEBENCH_PATH, assuming an old codebench version.'.format(toolchains_path))
        else:
            bb.fatal('Expected toolchains path `{}` does not exist, please ensure that CODEBENCH_PATH is set to a valid CodeBench installation'.format(toolchains_path))

    required_version = d.getVar('SOURCERY_VERSION_REQUIRED', True)
    if required_version:
        required_version = required_version.split('-', 1)[0]

    subdirs = os.listdir(toolchains_path)
    triplets, toolchain_subdir = [], None
    for triplet in d.getVar('EXTERNAL_TARGET_SYSTEMS', True).split():
        if required_version:
            expected_subdir = triplet + '.' + required_version
            if expected_subdir in subdirs:
                triplets.append(triplet)
                toolchain_subdir = expected_subdir
        else:
            for subdir in subdirs:
                if subdir.startswith(triplet + '.'):
                    triplets.append(triplet)
                    toolchain_subdir = subdir

        if triplets:
            break

    if len(triplets) > 1:
        bb.fatal('Error: unable to determine which toolchain to use, as multiple are available ({}). Please set EXTERNAL_TOOLCHAIN manually to the appropriate path in `{}`'.format(', '.join(triplets), toolchains_path))
    elif not triplets:
        bb.fatal('Unable to locate appropriate toolchain in `{}`, please set EXTERNAL_TOOLCHAIN to the correct toolchain path, or install the required CodeBench version'.format(toolchains_path))
    else:
        d.setVar('EXTERNAL_TARGET_SYS', triplets[0])
        d.setVar('EXTERNAL_TOOLCHAIN', os.path.join(toolchains_path, toolchain_subdir))
}
codebench_check[eventmask] = "bb.event.ConfigParsed"
addhandler codebench_check
