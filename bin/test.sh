#!/bin/bash

set -e

./bin/build.sh

docker run -p 9000:8080 past-lives-api