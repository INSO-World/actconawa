#!/bin/bash
if [[ ! -f openapi-generator-cli.jar ]]
then
    GENERATOR_VERSION=7.3.0
    echo "Downloading OpenApi Generator from maven repo."
    wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/$GENERATOR_VERSION/openapi-generator-cli-$GENERATOR_VERSION.jar \
        -O openapi-generator-cli.jar
fi

java -jar openapi-generator-cli.jar generate \
    -g typescript-angular \
    -i src/api-docs.json \
    -o src/api \
    --additional-properties=supportsES6=true
git add src/api/
