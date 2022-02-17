package com.infoledger.aggregation.enclave.client;

/** Exception indicating an issue while trying to aggregate data within Enclave app. */
public class EnclaveAggregationException extends RuntimeException {

  public EnclaveAggregationException(String message) {
    super(message);
  }

  public EnclaveAggregationException(String message, Throwable cause) {
    super(message, cause);
  }
}
