#!/bin/sh

# Assign an IP address to local loopback
ifconfig lo 127.0.0.1

java -cp /app/resources:/app/classes:/app/libs/* com.infoledger.crypto.enclave.app.EnclaveApplication --region $AWS_REGION --enclave-port $APP_PORT --proxy-port $VSOCK_PORT --operations $OPERATIONS --reattempts-number $REATTEMPTS_NUMBER