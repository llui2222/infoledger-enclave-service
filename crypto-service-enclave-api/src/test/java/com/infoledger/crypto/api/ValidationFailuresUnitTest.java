package com.infoledger.crypto.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationFailuresUnitTest {

  public static final String FAILURE_TYPE = "failureType";
  public static final String FAILURE_REASON = "failureReason";

  @Test
  void testCanBeCreatedHavingEntry() {
    // When
    ValidationFailures validationFailures = new ValidationFailures(FAILURE_TYPE, FAILURE_REASON);

    // Then
    assertTrue(validationFailures.containsKey(FAILURE_TYPE));
    assertSame(FAILURE_REASON, validationFailures.get(FAILURE_TYPE));
  }

  @Test
  void testEntityCanBeAddedToValidationFailures() {
    // Given
    ValidationFailures validationFailures = new ValidationFailures();
    assertNotNull(validationFailures);

    // When
    validationFailures.addFailure(FAILURE_TYPE, FAILURE_REASON);

    // Then
    assertTrue(validationFailures.containsKey(FAILURE_TYPE));
    assertSame(FAILURE_REASON, validationFailures.get(FAILURE_TYPE));
  }
}
