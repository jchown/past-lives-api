#!/bin/bash

set -e

./bin/build.sh

echo "Logging in to AWS"

repo="368159677168.dkr.ecr.eu-west-2.amazonaws.com/past-lives-api"
hash=$(git rev-parse --short HEAD)

aws="docker run --rm -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"
$aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin 368159677168.dkr.ecr.eu-west-2.amazonaws.com

echo "Tagging past-lives-api"

docker tag past-lives-api "$repo:$hash"
docker tag past-lives-api "$repo:latest"

echo "Pushing past-lives-api"

docker push "$repo:$hash"
docker push "$repo:latest"

echo "Switch endpoint to $repo:$hash"

$aws lambda update-function-code --function-name past-lives-api --image-uri "$repo:$hash" --region eu-west-2
