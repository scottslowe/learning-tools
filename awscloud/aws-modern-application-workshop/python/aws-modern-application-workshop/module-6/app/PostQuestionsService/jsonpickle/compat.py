from __future__ import absolute_import, division, unicode_literals
import sys
import types
import base64

PY_MAJOR = sys.version_info[0]
PY2 = PY_MAJOR == 2
PY3 = PY_MAJOR == 3

class_types = type,

if PY3:
    import queue
    import builtins
    string_types = (str,)
    numeric_types = (int, float)
    ustr = str
    from base64 import encodebytes, decodebytes
else:
    queue = __import__('Queue')
    builtins = __import__('__builtin__')
    string_types = (builtins.basestring,)
    numeric_types = (int, float, builtins.long)
    ustr = builtins.unicode
    encodebytes = base64.encodestring
    decodebytes = base64.decodestring
    class_types += types.ClassType,


def iterator(class_):
    if PY2 and hasattr(class_, '__next__'):
        class_.next = class_.__next__
    return class_
