import os
import json
from random import Random

from pkg_resources import resource_filename
from .sampling_rule import SamplingRule
from ..exceptions.exceptions import InvalidSamplingManifestError


with open(resource_filename(__name__, 'default_sampling_rule.json')) as f:
    default_sampling_rule = json.load(f)


class DefaultSampler(object):
    """
    The default sampler that holds either custom sampling rules
    or default sampling rules. Every time before the X-Ray recorder
    generates a segment, it calculates if this segment is sampled
    or not.
    """
    def __init__(self, rules=default_sampling_rule):
        """
        :param dict rules: a dict that defines custom sampling rules.
        An example configuration:
        {
            "version": 1,
            "rules": [
                {
                    "description": "Player moves.",
                    "service_name": "*",
                    "http_method": "*",
                    "url_path": "/api/move/*",
                    "fixed_target": 0,
                    "rate": 0.05
                }
            ],
            "default": {
                "fixed_target": 1,
                "rate": 0.1
            }
        }
        This example defines one custom rule and a default rule.
        The custom rule applies a five-percent sampling rate with no minimum
        number of requests to trace for paths under /api/move/. The default
        rule traces the first request each second and 10 percent of additional requests.
        The SDK applies custom rules in the order in which they are defined.
        If a request matches multiple custom rules, the SDK applies only the first rule.
        """
        version = rules.get('version', None)
        if version != 1:
            raise InvalidSamplingManifestError('Manifest version: %s is not supported.', version)

        if 'default' not in rules:
            raise InvalidSamplingManifestError('A default rule must be provided.')

        self._default_rule = SamplingRule(rule_dict=rules['default'],
                                          default=True)

        self._rules = []
        if 'rules' in rules:
            for rule in rules['rules']:
                self._rules.append(SamplingRule(rule))

        self._random = Random()

    def should_trace(self, service_name=None, method=None, path=None):
        """
        Return True if the sampler decide to sample based on input
        information and sampling rules. It will first check if any
        custom rule should be applied, if not it falls back to the
        default sampling rule.

        All optional arugments are extracted from incoming requests by
        X-Ray middleware to perform path based sampling.
        """
        if service_name and method and path:
            for rule in self._rules:
                if rule.applies(service_name, method, path):
                    return self._should_trace(rule)

        return self._should_trace(self._default_rule)

    def _should_trace(self, sampling_rule):

        if sampling_rule.reservoir.take():
            return True
        else:
            return self._random.random() < sampling_rule.rate
