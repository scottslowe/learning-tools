import copy

from .entity import Entity
from ..exceptions.exceptions import SegmentNotFoundException


class Subsegment(Entity):
    """
    The work done in a single segment can be broke down into subsegments.
    Subsegments provide more granular timing information and details about
    downstream calls that your application made to fulfill the original request.
    A subsegment can contain additional details about a call to an AWS service,
    an external HTTP API, or an SQL database.
    """
    def __init__(self, name, namespace, segment):
        """
        Create a new subsegment.

        :param str name: Subsegment name is required.
        :param str namespace: The namespace of the subsegment. Currently
            support `aws`, `remote` and `local`.
        :param Segment segment: The parent segment
        """
        super(Subsegment, self).__init__(name)

        if not segment:
            raise SegmentNotFoundException("A parent segment is required for creating subsegments.")

        self.parent_segment = segment
        self.trace_id = segment.trace_id

        self.type = 'subsegment'
        self.namespace = namespace

        self.sql = {}

    def add_subsegment(self, subsegment):
        """
        Add input subsegment as a child subsegment and increment
        reference counter and total subsegments counter of the
        parent segment.
        """
        super(Subsegment, self).add_subsegment(subsegment)
        self.parent_segment.increment()

    def remove_subsegment(self, subsegment):
        """
        Remove input subsegment from child subsegemnts and
        decrement parent segment total subsegments count.

        :param Subsegment: subsegment to remove.
        """
        super(Subsegment, self).remove_subsegment(subsegment)
        self.parent_segment.decrement_subsegments_size()

    def close(self, end_time=None):
        """
        Close the trace entity by setting `end_time`
        and flip the in progress flag to False. Also decrement
        parent segment's ref counter by 1.

        :param int end_time: Epoch in seconds. If not specified
            current time will be used.
        """
        super(Subsegment, self).close(end_time)
        self.parent_segment.decrement_ref_counter()

    def set_sql(self, sql):
        """
        Set sql related metadata. This function is used by patchers
        for database connectors and is not recommended to
        invoke manually.

        :param dict sql: sql related metadata
        """
        self.sql = sql

    def __getstate__(self):

        properties = copy.copy(self.__dict__)
        super(Subsegment, self)._delete_empty_properties(properties)

        del properties['parent_segment']
        if not self.sql:
            del properties['sql']
        return properties
