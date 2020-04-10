var jsonObject = xml2Json(pm.response.text());
pm.environment.set("sshKeyName", jsonObject.DescribeKeyPairsResponse.keySet.item[0].keyName);
