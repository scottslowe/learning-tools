import logging
import importlib

log = logging.getLogger(__name__)

SUPPORTED_MODULES = (
    'botocore',
    'pynamodb',
    'requests',
    'sqlite3',
    'mysql',
    'httplib',
    'pymongo',
    'psycopg2',
)

NO_DOUBLE_PATCH = (
    'botocore',
    'pynamodb',
    'requests',
    'sqlite3',
    'mysql',
    'pymongo',
    'psycopg2',
)

_PATCHED_MODULES = set()


def patch_all(double_patch=False):
    if double_patch:
        patch(SUPPORTED_MODULES, raise_errors=False)
    else:
        patch(NO_DOUBLE_PATCH, raise_errors=False)


def patch(modules_to_patch, raise_errors=True):
    modules = set()
    for module_to_patch in modules_to_patch:
        # boto3 depends on botocore and patching botocore is sufficient
        if module_to_patch == 'boto3':
            modules.add('botocore')
        # aioboto3 depends on aiobotocore and patching aiobotocore is sufficient
        # elif module_to_patch == 'aioboto3':
        #     modules.add('aiobotocore')
        # pynamodb requires botocore to be patched as well
        elif module_to_patch == 'pynamodb':
            modules.add('botocore')
            modules.add(module_to_patch)
        else:
            modules.add(module_to_patch)
    unsupported_modules = modules - set(SUPPORTED_MODULES)
    if unsupported_modules:
        raise Exception('modules %s are currently not supported for patching'
                        % ', '.join(unsupported_modules))

    for m in modules:
        _patch_module(m, raise_errors)


def _patch_module(module_to_patch, raise_errors=True):
    try:
        _patch(module_to_patch)
    except Exception:
        if raise_errors:
            raise
        log.debug('failed to patch module %s', module_to_patch)


def _patch(module_to_patch):

    path = 'aws_xray_sdk.ext.%s' % module_to_patch

    if module_to_patch in _PATCHED_MODULES:
        log.debug('%s already patched', module_to_patch)

    imported_module = importlib.import_module(path)
    imported_module.patch()

    _PATCHED_MODULES.add(module_to_patch)
    log.info('successfully patched module %s', module_to_patch)
