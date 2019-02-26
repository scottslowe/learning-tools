import json
import boto3
import uuid

from pymaging import Image

print('Loading function')

DEFAULT_MAX_WIDTH  = 200;
DEFAULT_MAX_HEIGHT = 200;
DDB_TABLE = 'images';

s3 = boto3.resource('s3')

def lambda_handler(event, context):
    print("Received event: " + json.dumps(event, indent=2))

    srcBucket = event.Records[0].s3.bucket.name;
    srcKey = event.Records[0].s3.object.key;
    dstBucket = srcBucket;
    dstKey = 'thumbs/' + srcKey;

    bucket = s3.Bucket(srcBucket)
    imageObject = bucket.Object(srcKey)

    metadata = image.metadata

    if 'width' in metadata:
        max_width = metadata['width']
    else:
        max_width = DEFAULT_MAX_WIDTH

    if 'height' in metadata:
        max_height = metadata['height']
    else:
        max_height = DEFAULT_MAX_HEIGHT

    scalingFactor = min(max_width / size.width, max_height / size.height)

    width  = scalingFactor * size.width
    height = scalingFactor * size.height

    random_file_name = uuid.uuid4() + srcKey
    image_path = '/tmp/' + random_file_name
    thumbnail_path = '/tmp/resized-' + random_file_name

    s3.download_file(srcBucket, srcKey, image_path)
    resize_image(image_path, thumbnail_path)

    with Image.open_from_path(image_path) as image:
        image.img.resize(width, height)
        image.save(thumbnail_path)

    s3.upload_file(thumbnail_path, dstBucket, dstKey)
