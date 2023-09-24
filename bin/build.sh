#!/bin/bash

echo "Building past-lives-api"

set -e

docker run -it --rm \
  --name past-lives-api \
  -v "$(pwd)":/src \
  -w /src \
  maven:3-eclipse-temurin-17 \
  mvn compile dependency:copy-dependencies -DincludeScope=runtime

echo "Packaging past-lives-api"

docker build -t past-lives-api .