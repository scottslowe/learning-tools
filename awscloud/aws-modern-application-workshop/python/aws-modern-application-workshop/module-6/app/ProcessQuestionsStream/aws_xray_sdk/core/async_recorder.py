import time

import wrapt

from aws_xray_sdk.core.recorder import AWSXRayRecorder
from aws_xray_sdk.core.utils import stacktrace


class AsyncAWSXRayRecorder(AWSXRayRecorder):
    def capture_async(self, name=None):
        """
        A decorator that records enclosed function in a subsegment.
        It only works with asynchronous functions.

        params str name: The name of the subsegment. If not specified
        the function name will be used.
        """

        @wrapt.decorator
        async def wrapper(wrapped, instance, args, kwargs):
            func_name = name
            if not func_name:
                func_name = wrapped.__name__

            result = await self.record_subsegment_async(
                wrapped, instance, args, kwargs,
                name=func_name,
                namespace='local',
                meta_processor=None,
            )

            return result

        return wrapper

    async def record_subsegment_async(self, wrapped, instance, args, kwargs, name,
                                      namespace, meta_processor):

        subsegment = self.begin_subsegment(name, namespace)

        exception = None
        stack = None
        return_value = None

        try:
            return_value = await wrapped(*args, **kwargs)
            return return_value
        except Exception as e:
            exception = e
            stack = stacktrace.get_stacktrace(limit=self._max_trace_back)
            raise
        finally:
            # No-op if subsegment is `None` due to `LOG_ERROR`.
            if subsegment is not None:
                end_time = time.time()
                if callable(meta_processor):
                    meta_processor(
                        wrapped=wrapped,
                        instance=instance,
                        args=args,
                        kwargs=kwargs,
                        return_value=return_value,
                        exception=exception,
                        subsegment=subsegment,
                        stack=stack,
                    )
                elif exception:
                    if subsegment:
                        subsegment.add_exception(exception, stack)

                self.end_subsegment(end_time)
