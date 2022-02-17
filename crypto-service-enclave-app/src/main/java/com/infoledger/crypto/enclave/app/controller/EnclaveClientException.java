package com.infoledger.crypto.enclave.app.controller;

/** Exception indicating an issue while trying to handle request. */
public class EnclaveClientException extends RuntimeException {

  public EnclaveClientException(String message) {
    super(message);
  }
}
