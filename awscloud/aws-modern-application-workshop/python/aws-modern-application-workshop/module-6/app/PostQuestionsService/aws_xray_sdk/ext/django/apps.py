import logging

from django.apps import AppConfig

from .conf import settings
from .db import patch_db
from .templates import patch_template
from aws_xray_sdk.core import xray_recorder
from aws_xray_sdk.core.exceptions.exceptions import SegmentNameMissingException


log = logging.getLogger(__name__)


class XRayConfig(AppConfig):
    name = 'aws_xray_sdk.ext.django'

    def ready(self):
        """
        Configure global XRay recorder based on django settings
        under XRAY_RECORDER namespace.
        This method could be called twice during server startup
        because of base command and reload command.
        So this function must be idempotent
        """
        if not settings.AWS_XRAY_TRACING_NAME:
            raise SegmentNameMissingException('Segment name is required.')

        xray_recorder.configure(
            daemon_address=settings.AWS_XRAY_DAEMON_ADDRESS,
            sampling=settings.SAMPLING,
            sampling_rules=settings.SAMPLING_RULES,
            context_missing=settings.AWS_XRAY_CONTEXT_MISSING,
            plugins=settings.PLUGINS,
            service=settings.AWS_XRAY_TRACING_NAME,
            dynamic_naming=settings.DYNAMIC_NAMING,
            streaming_threshold=settings.STREAMING_THRESHOLD,
            max_trace_back=settings.MAX_TRACE_BACK,
        )

        # if turned on subsegment will be generated on
        # built-in database and template rendering
        if settings.AUTO_INSTRUMENT:
            try:
                patch_db()
            except Exception:
                log.debug('failed to patch Django built-in database')
            try:
                patch_template()
            except Exception:
                log.debug('failed to patch Django built-in template engine')
