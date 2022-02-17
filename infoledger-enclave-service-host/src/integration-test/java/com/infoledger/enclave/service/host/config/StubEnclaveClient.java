package com.infoledger.enclave.service.host.config;

import com.amazonaws.auth.AWSCredentials;
import com.infoledger.aggregation.enclave.client.EnclaveAggregationException;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.validation.api.ValidationResponse;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Stub implementation to use in test to prevent interaction with vsock.
 * Simply returns new data passed, no actual aggregation performed.
 */
public class StubEnclaveClient extends EnclaveClient {

  public static final Map<String, ValidationFailures> VALIDATION_FAILURES_MAP = Map.of("someFileName.xlsx", new ValidationFailures("columnMissed",
      "Required column company name is missed.")
      .addFailure("missedData", "Its not allowed to provide null or empty data"));
  private static boolean isValidationFails = false;

  public StubEnclaveClient() {
    super(0, 0);
  }

  @Override
  public byte[] aggregate(AWSCredentials credentials, String kmsKeyArn, byte[] newData, @Nullable byte[] currentData) throws EnclaveAggregationException {
    return newData;
  }

  @Override
  public ValidationResponse validate(AWSCredentials credentials, String kmsKeyArn, byte[] data) {
    if (isValidationFails) {
      return ValidationResponse.failed(VALIDATION_FAILURES_MAP);
    }
    return ValidationResponse.ok();
  }

  public static void setValidationFails(boolean validationFails) {
    isValidationFails = validationFails;
  }
}
