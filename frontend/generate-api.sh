#!/bin/bash
if [[ ! -f openapi-generator-cli.jar ]]
then
    echo "Downloading OpenApi Generator from maven repo."
    wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar -O openapi-generator-cli.jar
fi

java -jar openapi-generator-cli.jar generate -g typescript-angular -i src/api-docs.json -o src/api
