#!/bin/bash

ENCLAVE_PORT=5000 &&
ENCLAVE_CID=10 &&
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )" &&

cd "$SCRIPT_DIR"/.. &&
git pull &&
mvn -pl infoledger-enclave-service-host clean install -DskipTests --quiet -U --settings settings.xml &&
cd ./infoledger-enclave-service-host &&
sed -i "s/CID_ENCLAVE/$ENCLAVE_CID/g" Dockerfile &&
sed -i "s/PORT_ENCLAVE/$ENCLAVE_PORT/g" Dockerfile &&
docker build --tag=infoledger-enclave-service-host-base:latest . &&
docker run -p8081:8080 infoledger-enclave-service-host-base:latest
