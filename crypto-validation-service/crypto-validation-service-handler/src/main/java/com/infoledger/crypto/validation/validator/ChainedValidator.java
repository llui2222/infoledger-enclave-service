package com.infoledger.crypto.validation.validator;

import com.infoledger.crypto.validation.ValidationResult;
import com.infoledger.crypto.validation.ValidationRule;
import java.util.ArrayList;
import java.util.Arrays;

/** Chained validator iterating over validation rules and failing of first failure. */
public class ChainedValidator extends ArrayList<ValidationRule> {

  public ChainedValidator(ValidationRule... chain) {
    addAll(Arrays.asList(chain));
  }

  public ValidationResult validate(byte[] data) {
    for (ValidationRule rule : this) {
      ValidationResult result = rule.validate(data);

      if (!result.isValid()) {
        return result;
      }
    }

    return ValidationResult.ok();
  }
}
