import logging
import os
import binascii
import time
import string

import jsonpickle

from ..utils.compat import annotation_value_types, string_types
from .throwable import Throwable
from . import http
from ..exceptions.exceptions import AlreadyEndedException


log = logging.getLogger(__name__)

# Valid characters can be found at http://docs.aws.amazon.com/xray/latest/devguide/xray-api-segmentdocuments.html
_common_invalid_name_characters = '?;*()!$~^<>'
_valid_annotation_key_characters = string.ascii_letters + string.digits + '_'


class Entity(object):
    """
    The parent class for segment/subsegment. It holds common properties
    and methods on segment and subsegment.
    """
    def __init__(self, name):

        # required attributes
        self.id = self._generate_random_id()
        self.name = name
        self.name = ''.join([c for c in name if c not in _common_invalid_name_characters])
        self.start_time = time.time()
        self.parent_id = None

        if self.name != name:
            log.warning("Removing Segment/Subsugment Name invalid characters from {}.".format(name))

        # sampling
        self.sampled = True

        # state
        self.in_progress = True

        # meta fields
        self.http = {}
        self.annotations = {}
        self.metadata = {}
        self.aws = {}
        self.cause = {}

        # child subsegments
        # list is thread-safe
        self.subsegments = []

    def close(self, end_time=None):
        """
        Close the trace entity by setting `end_time`
        and flip the in progress flag to False.

        :param int end_time: Epoch in seconds. If not specified
            current time will be used.
        """
        self._check_ended()

        if end_time:
            self.end_time = end_time
        else:
            self.end_time = time.time()
        self.in_progress = False

    def add_subsegment(self, subsegment):
        """
        Add input subsegment as a child subsegment.
        """
        self._check_ended()
        subsegment.parent_id = self.id
        self.subsegments.append(subsegment)

    def remove_subsegment(self, subsegment):
        """
        Remove input subsegment from child subsegments.
        """
        self.subsegments.remove(subsegment)

    def put_http_meta(self, key, value):
        """
        Add http related metadata.

        :param str key: Currently supported keys are:
            * url
            * method
            * user_agent
            * client_ip
            * status
            * content_length
        :param value: status and content_length are int and for other
            supported keys string should be used.
        """
        self._check_ended()

        if value is None:
            return

        if key == http.STATUS:
            if isinstance(value, string_types):
                value = int(value)
            self.apply_status_code(value)

        if key in http.request_keys:
            if 'request' not in self.http:
                self.http['request'] = {}
            self.http['request'][key] = value
        elif key in http.response_keys:
            if 'response' not in self.http:
                self.http['response'] = {}
            self.http['response'][key] = value
        else:
            log.warning("ignoring unsupported key %s in http meta.", key)

    def put_annotation(self, key, value):
        """
        Annotate segment or subsegment with a key-value pair.
        Annotations will be indexed for later search query.

        :param str key: annotation key
        :param object value: annotation value. Any type other than
            string/number/bool will be dropped
        """
        self._check_ended()

        if not isinstance(key, string_types):
            log.warning("ignoring non string type annotation key with type %s.", type(key))
            return

        if not isinstance(value, annotation_value_types):
            log.warning("ignoring unsupported annotation value type %s.", type(value))
            return

        if any(character not in _valid_annotation_key_characters for character in key):
            log.warning("ignoring annnotation with unsupported characters in key: '%s'.", key)
            return

        self.annotations[key] = value

    def put_metadata(self, key, value, namespace='default'):
        """
        Add metadata to segment or subsegment. Metadata is not indexed
        but can be later retrieved by BatchGetTraces API.

        :param str namespace: optional. Default namespace is `default`.
            It must be a string and prefix `AWS.` is reserved.
        :param str key: metadata key under specified namespace
        :param object value: any object that can be serialized into JSON string
        """
        self._check_ended()

        if not isinstance(namespace, string_types):
            log.warning("ignoring non string type metadata namespace")
            return

        if namespace.startswith('AWS.'):
            log.warning("Prefix 'AWS.' is reserved, drop metadata with namespace %s", namespace)
            return

        if self.metadata.get(namespace, None):
            self.metadata[namespace][key] = value
        else:
            self.metadata[namespace] = {key: value}

    def set_aws(self, aws_meta):
        """
        set aws section of the entity.
        This method is called by global recorder and botocore patcher
        to provide additonal information about AWS runtime.
        It is not recommended to manually set aws section.
        """
        self._check_ended()
        self.aws = aws_meta

    def add_throttle_flag(self):
        self.throttle = True

    def add_fault_flag(self):
        self.fault = True

    def add_error_flag(self):
        self.error = True

    def apply_status_code(self, status_code):
        """
        When a trace entity is generated under the http context,
        the status code will affect this entity's fault/error/throttle flags.
        Flip these flags based on status code.
        """
        self._check_ended()
        if not status_code:
            return

        if status_code >= 500:
            self.add_fault_flag()
        elif status_code == 429:
            self.add_throttle_flag()
            self.add_error_flag()
        elif status_code >= 400:
            self.add_error_flag()

    def add_exception(self, exception, stack, remote=False):
        """
        Add an exception to trace entities.

        :param Exception exception: the catched exception.
        :param list stack: the output from python built-in
            `traceback.extract_stack()`.
        :param bool remote: If False it means it's a client error
            instead of a downstream service.
        """
        self._check_ended()
        self.add_fault_flag()

        if hasattr(exception, '_recorded'):
            setattr(self, 'cause', getattr(exception, '_cause_id'))
            return

        exceptions = []
        exceptions.append(Throwable(exception, stack, remote))

        self.cause['exceptions'] = exceptions
        self.cause['working_directory'] = os.getcwd()

    def serialize(self):
        """
        Serialize to JSON document that can be accepted by the
        X-Ray backend service. It uses jsonpickle to perform
        serialization.
        """
        try:
            return jsonpickle.encode(self, unpicklable=False)
        except Exception:
            log.exception("got an exception during serialization")

    def _delete_empty_properties(self, properties):
        """
        Delete empty properties before serialization to avoid
        extra keys with empty values in the output json.
        """
        if not self.parent_id:
            del properties['parent_id']
        if not self.subsegments:
            del properties['subsegments']
        if not self.aws:
            del properties['aws']
        if not self.http:
            del properties['http']
        if not self.cause:
            del properties['cause']
        if not self.annotations:
            del properties['annotations']
        if not self.metadata:
            del properties['metadata']

        del properties['sampled']

    def _check_ended(self):
        if not self.in_progress:
            raise AlreadyEndedException("Already ended segment and subsegment cannot be modified.")

    def _generate_random_id(self):
        """
        Generate a random 16-digit hex str.
        This is used for generating segment/subsegment id.
        """
        return binascii.b2a_hex(os.urandom(8)).decode('utf-8')
