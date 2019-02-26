function getOptionalAttribute(item, attr, type) {
  return (item[attr] !== undefined) ? item[attr][type] : undefined;
}

exports.mapImage = function(item) {
  return {
    'id': item.id.S,
    'version': parseInt(item.version.N, 10),
    'state': item.state.S,
    'rawS3Key': getOptionalAttribute(item, 'rawS3Key', 'S'),
    'processedS3Key': getOptionalAttribute(item, 'processedS3Key', 'S'),
    'processedImage': (item.processedS3Key !== undefined) ? ('https://s3.amazonaws.com/' + process.env.ImageBucket + '/' + item.processedS3Key.S) : undefined
  };
};
