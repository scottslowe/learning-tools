var jsonObject = xml2Json(pm.response.text());
pm.environment.set("firstAz", jsonObject.DescribeAvailabilityZonesResponse.availabilityZoneInfo.item[0].zoneName);
