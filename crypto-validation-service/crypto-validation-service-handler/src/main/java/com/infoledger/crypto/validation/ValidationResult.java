package com.infoledger.crypto.validation;

import com.infoledger.crypto.api.ValidationFailures;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResult {

  enum Status {
    OK,
    FAILED
  }

  private Status status;
  private Map<String, ValidationFailures> validationFailures;

  public static ValidationResult ok() {
    return new ValidationResult(Status.OK, null);
  }

  public static ValidationResult failed(Map<String, ValidationFailures> validationFailures) {
    return new ValidationResult(Status.FAILED, validationFailures);
  }

  public boolean isValid() {
    return this.status == Status.OK;
  }
}
