echo "Building with travis commit of $BUILD_NAME ..."
mvn clean package docker:build
