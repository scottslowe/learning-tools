import copy
import os
import binascii
import logging

from ..utils.compat import string_types

log = logging.getLogger(__name__)


class Throwable(object):
    """
    An object recording exception infomation under trace entity
    `cause` section. The information includes the stack trace,
    working directory and message from the original exception.
    """
    def __init__(self, exception, stack, remote=False):
        """
        :param Exception exception: the catched exception.
        :param list stack: the formatted stack trace gathered
            through `traceback` module.
        :param bool remote: If False it means it's a client error
            instead of a downstream service.
        """
        self.id = binascii.b2a_hex(os.urandom(8)).decode('utf-8')

        try:
            message = str(exception)
            # in case there is an exception cannot be converted to str
        except Exception:
            message = None

        # do not record non-string exception message
        if isinstance(message, string_types):
            self.message = message

        self.type = type(exception).__name__
        self.remote = remote

        try:
            self._normalize_stack_trace(stack)
        except Exception:
            self.stack = None
            log.warning("can not parse stack trace string, ignore stack field.")

        if exception:
            setattr(exception, '_recorded', True)
            setattr(exception, '_cause_id', self.id)

    def _normalize_stack_trace(self, stack):
        if not stack:
            return None

        self.stack = []

        for entry in stack:
            path = entry[0]
            line = entry[1]
            label = entry[2]
            if 'aws_xray_sdk/' in path:
                continue

            normalized = {}
            normalized['path'] = os.path.basename(path).replace('\"', ' ').strip()
            normalized['line'] = line
            normalized['label'] = label.strip()

            self.stack.append(normalized)

    def __getstate__(self):
        properties = copy.copy(self.__dict__)

        if not self.stack:
            del properties['stack']

        return properties
