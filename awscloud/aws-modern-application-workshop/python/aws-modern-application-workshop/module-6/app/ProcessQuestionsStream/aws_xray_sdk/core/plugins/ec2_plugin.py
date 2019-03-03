import logging
import requests

log = logging.getLogger(__name__)

SERVICE_NAME = 'ec2'
ORIGIN = 'AWS::EC2::Instance'


def initialize():
    """
    Try to get EC2 instance-id and AZ if running on EC2
    by querying http://169.254.169.254/latest/meta-data/.
    If not continue.
    """
    global runtime_context

    try:
        runtime_context = {}

        r = requests.get('http://169.254.169.254/latest/meta-data/instance-id', timeout=1)
        runtime_context['instance_id'] = r.text

        r = requests.get('http://169.254.169.254/latest/meta-data/placement/availability-zone', timeout=1)
        runtime_context['availability_zone'] = r.text

    except Exception:
        runtime_context = None
        log.warning("failed to get ec2 instance metadata.")
