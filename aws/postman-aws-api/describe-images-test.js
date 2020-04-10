var jsonObject = xml2Json(pm.response.text());
pm.environment.set("imageId", jsonObject.DescribeImagesResponse.imagesSet.item.imageId);
