import logging
import importlib

from django.db import connections

from aws_xray_sdk.ext.dbapi2 import XRayTracedCursor

log = logging.getLogger(__name__)


def patch_db():

    for conn in connections.all():
        module = importlib.import_module(conn.__module__)
        _patch_conn(getattr(module, conn.__class__.__name__))


def _patch_conn(conn):

    attr = '_xray_original_cursor'

    if hasattr(conn, attr):
        log.debug('django built-in db already patched')
        return

    setattr(conn, attr, conn.cursor)

    meta = {}

    if hasattr(conn, 'vendor'):
        meta['database_type'] = conn.vendor

    def cursor(self, *args, **kwargs):

        host = None
        user = None

        if hasattr(self, 'settings_dict'):
            settings = self.settings_dict
            host = settings.get('HOST', None)
            user = settings.get('USER', None)

        if host:
            meta['name'] = host
        if user:
            meta['user'] = user

        return XRayTracedCursor(
            self._xray_original_cursor(*args, **kwargs), meta)

    conn.cursor = cursor
