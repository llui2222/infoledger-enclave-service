package com.infoledger.crypto.api;

import java.util.HashMap;

public class ValidationFailures extends HashMap<String, String> {

  public ValidationFailures() {
    // Empty ctor.
  }

  public ValidationFailures(String failureType, String failureMessage) {
    this.put(failureType, failureMessage);
  }

  public ValidationFailures addFailure(String failureType, String failureMessage) {
    this.put(failureType, failureMessage);
    return this;
  }
}
