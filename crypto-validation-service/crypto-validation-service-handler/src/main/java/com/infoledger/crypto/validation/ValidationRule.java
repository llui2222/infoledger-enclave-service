package com.infoledger.crypto.validation;

public interface ValidationRule {

  /**
   * Validates the specified data
   *
   * @param data Data to validate
   * @return Validation result
   */
  ValidationResult validate(byte[] data);
}
