from __future__ import print_function

import numpy
import cv2
import json
import urllib2
import uuid
import datetime
import boto3

print('Loading function')

dstBucket = 'danilop-eventdrivenapps'
dstPrefix = 'tmp/'
outputDomain = 'eventdrivenapps.com'

cascPath = 'share/OpenCV/haarcascades/haarcascade_frontalface_alt.xml'

s3 = boto3.resource('s3')
faceCascade = cv2.CascadeClassifier(cascPath)

def lambda_handler(event, context):
    print('Received event: ' + json.dumps(event, indent=2))
    imageUrl = event['imageUrl']

    imageFile = urllib2.urlopen(imageUrl)

    imageBytes = numpy.asarray(bytearray(imageFile.read()), dtype=numpy.uint8)
    image = cv2.imdecode(imageBytes, cv2.CV_LOAD_IMAGE_UNCHANGED)

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(30, 30),
        flags = cv2.cv.CV_HAAR_SCALE_IMAGE
    )

    for (x, y, w, h) in faces:
        cv2.rectangle(image, (x, y), (x+w, y+h), (255, 255, 255), 2)

    r, outputImage = cv2.imencode('.jpg', image)
    if False==r:
        raise Exception('Error encoding image')

    dstKey = dstPrefix + datetime.datetime.now().strftime('%Y%m%d%H%M%S') + '-' + str(uuid.uuid4()) + '.jpg'

    s3.Bucket(dstBucket).put_object(Key=dstKey,
        Body=outputImage.tostring(),
        ContentType='image/jpeg'
    )

    outputUrl = 'https://' + outputDomain + '/' + dstKey

    result = { 'faces': len(faces), 'outputUrl': outputUrl }

    return result
