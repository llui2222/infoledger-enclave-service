package com.infoledger.enclave.service.host.stubclient;

import com.amazonaws.auth.AWSCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StubEnclaveValidationClient {
    public byte[] validate(AWSCredentials awsCredentials, String kmsKeyArn, byte[] fileBytes) {
        log.debug("AWS Credentials: " + awsCredentials);
        log.debug("KMS key ARN: : " + kmsKeyArn);

        return fileBytes;
    }
}
