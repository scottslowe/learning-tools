var jsonObject = xml2Json(pm.response.text());
pm.environment.set("defaultVpcId", jsonObject.DescribeVpcsResponse.vpcSet.item.vpcId);
