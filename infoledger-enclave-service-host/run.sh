#!/bin/sh

java -jar -Dspring.profiles.active=cloud -Denclave.cid=$ENCLAVE_CID_ARG -Denclave.port=$ENCLAVE_PORT_ARG enclave-service-host.jar