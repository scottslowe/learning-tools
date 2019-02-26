# How to create a new etherpad.zip

1. On Amazon Linux (see version here http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/concepts.platforms.html#concepts.platforms.nodejs)
1. Download latest version for Linux/Max from http://etherpad.org/#download
1. cd into the unzipped directory
1. Create `.ebextensions/custom.config` with the following content (make sure NodeVersion is available in latest EB environment):
```
option_settings:
  aws:elasticbeanstalk:container:nodejs: 
    NodeCommand: "bin/run.sh"
    NodeVersion: "8.12.0"
```
1. Create `src/.npmrc` with the following content:
```
unsafe-perm = true
```
1. Copy `settings.json.template` to `settings.json` and change port to 8081
1. Run `zip -r etherpad.zip ./`
