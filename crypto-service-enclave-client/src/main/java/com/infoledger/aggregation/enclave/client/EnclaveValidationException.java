package com.infoledger.aggregation.enclave.client;

/** Exception indicating an issue while trying to validate data within Enclave app. */
public class EnclaveValidationException extends RuntimeException {

  public EnclaveValidationException(String message) {
    super(message);
  }

  public EnclaveValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
