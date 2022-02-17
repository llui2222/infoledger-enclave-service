package com.infoledger.crypto.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Response base class. */
@Getter
@EqualsAndHashCode
public class CryptoResponse {

  public enum Status {
    OK,
    FAILED
  }

  private final Status status;
  private final String message;

  public CryptoResponse(Status status, String message) {
    this.status = status;
    this.message = message;
  }

  public boolean isOk() {
    return status == Status.OK;
  }
}
