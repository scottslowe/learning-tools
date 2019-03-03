from .recorder import AWSXRayRecorder
from .patcher import patch_all, patch
from .utils.compat import PY35


if not PY35:
    xray_recorder = AWSXRayRecorder()
else:
    from .async_recorder import AsyncAWSXRayRecorder
    xray_recorder = AsyncAWSXRayRecorder()

__all__ = [
    'patch',
    'patch_all',
    'xray_recorder',
    'AWSXRayRecorder',
]
