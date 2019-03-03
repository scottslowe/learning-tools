from collections import namedtuple
import sys
import wrapt

from aws_xray_sdk.core import xray_recorder
from aws_xray_sdk.core.models import http
from aws_xray_sdk.core.exceptions.exceptions import SegmentNotFoundException
from aws_xray_sdk.ext.util import inject_trace_header, strip_url, unwrap

if sys.version_info >= (3, 0, 0):
    PY2 = False
    httplib_client_module = 'http.client'
    import http.client as httplib
else:
    PY2 = True
    httplib_client_module = 'httplib'
    import httplib


_XRAY_PROP = '_xray_prop'
_XRay_Data = namedtuple('xray_data', ['method', 'host', 'url'])
# A flag indicates whether this module is X-Ray patched or not
PATCH_FLAG = '__xray_patched'


def http_response_processor(wrapped, instance, args, kwargs, return_value,
                            exception, subsegment, stack):
    xray_data = getattr(instance, _XRAY_PROP, None)
    if not xray_data:
        return

    subsegment.put_http_meta(http.METHOD, xray_data.method)
    subsegment.put_http_meta(http.URL, xray_data.url)

    if return_value:
        subsegment.put_http_meta(http.STATUS, return_value.status)

        # propagate to response object
        xray_data = _XRay_Data('READ', xray_data.host, xray_data.url)
        setattr(return_value, _XRAY_PROP, xray_data)

    if exception:
        subsegment.add_exception(exception, stack)


def _xray_traced_http_getresponse(wrapped, instance, args, kwargs):
    if not PY2 and kwargs.get('buffering', False):
        # ignore py2 calls that fail as 'buffering` only exists in py2.
        return wrapped(*args, **kwargs)

    xray_data = getattr(instance, _XRAY_PROP, None)
    if not xray_data:
        return wrapped(*args, **kwargs)

    return xray_recorder.record_subsegment(
        wrapped, instance, args, kwargs,
        name=strip_url(xray_data.url),
        namespace='remote',
        meta_processor=http_response_processor,
    )


def http_send_request_processor(wrapped, instance, args, kwargs, return_value,
                                exception, subsegment, stack):
    xray_data = getattr(instance, _XRAY_PROP, None)
    if not xray_data:
        return

    # we don't delete the attr as we can have multiple reads
    subsegment.put_http_meta(http.METHOD, xray_data.method)
    subsegment.put_http_meta(http.URL, xray_data.url)

    if exception:
        subsegment.add_exception(exception, stack)


def _send_request(wrapped, instance, args, kwargs):
    def decompose_args(method, url, body, headers, encode_chunked=False):
        # skip httplib tracing for SDK built-in centralized sampling pollers
        if (('/GetSamplingRules' in args or '/SamplingTargets' in args) and
                type(instance).__name__ == 'botocore.awsrequest.AWSHTTPConnection'):
            return wrapped(*args, **kwargs)

        # Only injects headers when the subsegment for the outgoing
        # calls are opened successfully.
        subsegment = None
        try:
            subsegment = xray_recorder.current_subsegment()
        except SegmentNotFoundException:
            pass
        if subsegment:
            inject_trace_header(headers, subsegment)

        ssl_cxt = getattr(instance, '_context', None)
        scheme = 'https' if ssl_cxt and type(ssl_cxt).__name__ == 'SSLContext' else 'http'
        xray_url = '{}://{}{}'.format(scheme, instance.host, url)
        xray_data = _XRay_Data(method, instance.host, xray_url)
        setattr(instance, _XRAY_PROP, xray_data)

        # we add a segment here in case connect fails
        return xray_recorder.record_subsegment(
            wrapped, instance, args, kwargs,
            name=strip_url(xray_data.url),
            namespace='remote',
            meta_processor=http_send_request_processor
        )

    return decompose_args(*args, **kwargs)


def http_read_processor(wrapped, instance, args, kwargs, return_value,
                        exception, subsegment, stack):
    xray_data = getattr(instance, _XRAY_PROP, None)
    if not xray_data:
        return

    # we don't delete the attr as we can have multiple reads
    subsegment.put_http_meta(http.METHOD, xray_data.method)
    subsegment.put_http_meta(http.URL, xray_data.url)
    subsegment.put_http_meta(http.STATUS, instance.status)

    if exception:
        subsegment.add_exception(exception, stack)


def _xray_traced_http_client_read(wrapped, instance, args, kwargs):
    xray_data = getattr(instance, _XRAY_PROP, None)
    if not xray_data:
        return wrapped(*args, **kwargs)

    return xray_recorder.record_subsegment(
        wrapped, instance, args, kwargs,
        name=strip_url(xray_data.url),
        namespace='remote',
        meta_processor=http_read_processor
    )


def patch():
    """
    patch the built-in `urllib/httplib/httplib.client` methods for tracing.
    """
    if getattr(httplib, PATCH_FLAG, False):
        return
    # we set an attribute to avoid multiple wrapping
    setattr(httplib, PATCH_FLAG, True)

    wrapt.wrap_function_wrapper(
        httplib_client_module,
        'HTTPConnection._send_request',
        _send_request
    )

    wrapt.wrap_function_wrapper(
        httplib_client_module,
        'HTTPConnection.getresponse',
        _xray_traced_http_getresponse
    )

    wrapt.wrap_function_wrapper(
        httplib_client_module,
        'HTTPResponse.read',
        _xray_traced_http_client_read
    )


def unpatch():
    """
    Unpatch any previously patched modules.
    This operation is idempotent.
    """
    setattr(httplib, PATCH_FLAG, False)
    # _send_request encapsulates putrequest, putheader[s], and endheaders
    unwrap(httplib.HTTPConnection, '_send_request')
    unwrap(httplib.HTTPConnection, 'getresponse')
    unwrap(httplib.HTTPResponse, 'read')
