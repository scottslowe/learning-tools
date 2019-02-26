#!/bin/bash -ex

rm -rf build/
mkdir -p build/

(
cd worker/
zip -r ../build/worker.zip lib.js package.json worker.js .ebextensions/
)

(
cd server/
zip -r ../build/server.zip lib.js package.json server.js public/
)
