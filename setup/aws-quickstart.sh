#!/bin/bash

CMK_REGION=us-east-1 &&
PROXY_PORT=8443 &&
ENCLAVE_PORT=5000 &&
ENCLAVE_CID=10 &&
ENCLAVE_OPERATIONS="aggregation validation" &&
NUMBER_OF_CONNECT_ATTEMPTS=7 &&
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )" &&

## First, start vsock proxy
screen -d -m vsock-proxy $PROXY_PORT kms.$CMK_REGION.amazonaws.com 443 &&

## Next, checkout the most recent master, build Docker and Enclave images and start

cd "$SCRIPT_DIR"/.. &&
rm -f crypto-service-enclave-app.eif &&
git pull &&
sudo nitro-cli terminate-enclave --all &&
mvn clean install --quiet -U -DskipTests --settings settings.xml  &&
mvn -f crypto-service-enclave-app/pom.xml compile package jib:dockerBuild --settings settings.xml &&
sed -i "s/ENCLAVE_REGION/$CMK_REGION/g" crypto-service-enclave-app/Dockerfile &&
sed -i "s/ENCLAVE_PORT/$ENCLAVE_PORT/g" crypto-service-enclave-app/Dockerfile &&
sed -i "s/PROXY_PORT/$PROXY_PORT/g" crypto-service-enclave-app/Dockerfile &&
sed -i "s/ENCLAVE_OPERATIONS/$ENCLAVE_OPERATIONS/g" crypto-service-enclave-app/Dockerfile &&
sed -i "s/NUMBER_OF_CONNECT_ATTEMPTS/$NUMBER_OF_CONNECT_ATTEMPTS/g" crypto-service-enclave-app/Dockerfile &&
docker build crypto-service-enclave-app -t crypto-service-enclave-app &&
sudo nitro-cli build-enclave --docker-uri crypto-service-enclave-app:latest --output-file crypto-service-enclave-app.eif &&
sudo nitro-cli run-enclave --cpu-count 2 --memory 3072 --eif-path crypto-service-enclave-app.eif --enclave-cid $ENCLAVE_CID --debug-mode &&
sudo nitro-cli console --enclave-id $(nitro-cli describe-enclaves | jq -r ".[0].EnclaveID")