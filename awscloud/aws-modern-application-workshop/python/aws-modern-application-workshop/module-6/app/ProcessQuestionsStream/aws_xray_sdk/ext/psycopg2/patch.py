import re
import wrapt

from aws_xray_sdk.ext.dbapi2 import XRayTracedConn


def patch():

    wrapt.wrap_function_wrapper(
        'psycopg2',
        'connect',
        _xray_traced_connect
    )


def _xray_traced_connect(wrapped, instance, args, kwargs):

    conn = wrapped(*args, **kwargs)
    host = kwargs['host'] if 'host' in kwargs else re.search(r'host=(\S+)\b', args[0]).groups()[0]
    dbname = kwargs['dbname'] if 'dbname' in kwargs else re.search(r'dbname=(\S+)\b', args[0]).groups()[0]
    port = kwargs['port'] if 'port' in kwargs else re.search(r'port=(\S+)\b', args[0]).groups()[0]
    user = kwargs['user'] if 'user' in kwargs else re.search(r'user=(\S+)\b', args[0]).groups()[0]
    meta = {
        'database_type': 'PostgreSQL',
        'url': 'postgresql://{}@{}:{}/{}'.format(user, host, port, dbname),
        'user': user,
        'database_version': str(conn.server_version),
        'driver_version': 'Psycopg 2'
    }

    return XRayTracedConn(conn, meta)
