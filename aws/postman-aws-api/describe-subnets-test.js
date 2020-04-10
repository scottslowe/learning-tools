var jsonObject = xml2Json(pm.response.text());
pm.environment.set("SubnetId", jsonObject.DescribeSubnetsResponse.subnetSet.item.subnetId);
