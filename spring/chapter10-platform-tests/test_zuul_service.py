import unittest
import logging
import json
import string
import argparse
import os
import urllib
from httplib2 import Http

class TestZuulService(unittest.TestCase):

    def build_headers(self):
        return {'Content-Type': 'application/json; charset=UTF-8',
                'connection': 'close',
                'Accept': 'application/json',
                'Authorization': 'Bearer {}'.format(oauthtoken)}

    def call_zuul_service(self):
         targetUri = "http://{}:5555/routes".format(containerIP)
         http_obj = Http(".cache")
         (resp, content) = http_obj.request(
         uri=targetUri,
         method='GET',
         headers=self.build_headers())
         return resp,content

    def call_org_service(self):
         targetUri = "http://{}:5555/api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a".format(containerIP)
         http_obj = Http(".cache")
         (resp, content) = http_obj.request(
         uri=targetUri,
         method='GET',
         headers=self.build_headers())
         return resp,content

    def call_licensing_service(self):
         targetUri = "http://{}:5555/api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a".format(containerIP)
         http_obj = Http(".cache")
         (resp, content) = http_obj.request(
         uri=targetUri,
         method='GET',
         headers=self.build_headers())
         return resp,content

    def test_zuul_service_routes(self):
        (resp, content) = self.call_zuul_service()
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEquals("organizationservice", results["/api/organization/**"])
        self.assertEquals("licensingservice", results[ "/api/licensing/**"])
        self.assertEquals("authenticationservice", results["/api/auth/**"])
        self.assertEquals(3, len(results))

    def test_org_service(self):
        (resp, content) = self.call_org_service()
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("e254f8c-c442-4ebe-a82a-e2fc1d1ff78a", results["id"])
        self.assertEqual("customer-crm-co", results["name"])
        self.assertEqual("Mark Balster", results["contactName"])
        self.assertEqual("mark.balster@custcrmco.com", results["contactEmail"])
        self.assertEqual("823-555-1212", results["contactPhone"])

    def test_licensing_service(self):
        (resp, content) = self.call_licensing_service()
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a", results["licenseId"])
        self.assertEqual("e254f8c-c442-4ebe-a82a-e2fc1d1ff78a", results["organizationId"])
        self.assertEqual("Mark Balster", results["contactName"])
        self.assertEqual("mark.balster@custcrmco.com", results["contactEmail"])
        self.assertEqual("823-555-1212", results["contactPhone"])
        self.assertEqual("CustomerPro", results["productName"])

def retrieve_oauth_service():
    targetUri = "http://{}:5555/api/auth/oauth/token ".format(containerIP)
    http = Http(".cache")
    body = {'grant_type': 'password',
            'scope': 'webclient',
            'username':'william.woodward',
            'password':'password2'}

    content = http.request(
            uri=targetUri,
            method="POST",
            headers={'Content-type': 'application/x-www-form-urlencoded', 'Authorization': 'Basic ZWFnbGVleWU6dGhpc2lzc2VjcmV0'},
            body=urllib.urlencode(body))
    results = json.loads(content[1])
    return results.get("access_token")

if __name__ == '__main__':
    containerIP = os.getenv('CONTAINER_IP',"192.168.99.100")
    print "Running zuul service platform tests against container ip: {}".format(containerIP)
    oauthtoken = retrieve_oauth_service()
    print "OAuthToken successfully retrieved: {}".format(oauthtoken)
    unittest.main()
