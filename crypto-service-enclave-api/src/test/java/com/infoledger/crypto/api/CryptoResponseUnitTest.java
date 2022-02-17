package com.infoledger.crypto.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

class CryptoResponseUnitTest {

  private static final String SUCCESSFULLY_EXECUTED = "Successfully Executed";
  private static final byte[] DATA_BYTES = {0, 1};
  private static final Map<String, ValidationFailures> VALIDATION_FAILURES =
      Map.of("sheet1", new ValidationFailures("failed", "failureReason"));

  @Test
  void testSerDeOfDifferentResponseTypes() {
    // given
    CryptoResponse aggregationResponse =
        new AggregationResponse(DATA_BYTES, CryptoResponse.Status.OK, SUCCESSFULLY_EXECUTED);
    CryptoResponse validationResponse =
        new ValidationResponse(CryptoResponse.Status.FAILED, VALIDATION_FAILURES);

    // when && then
    // Aggregation response
    assertTrue(aggregationResponse.isOk());
    assertEquals(SUCCESSFULLY_EXECUTED, aggregationResponse.getMessage());
    assertEquals(CryptoResponse.Status.OK, aggregationResponse.getStatus());

    // Validation response
    assertFalse(validationResponse.isOk());
    assertNull(validationResponse.getMessage());
    assertEquals(CryptoResponse.Status.FAILED, validationResponse.getStatus());

    // given
    CryptoResponse aggregationResponseTwo =
        new AggregationResponse(DATA_BYTES, CryptoResponse.Status.OK, SUCCESSFULLY_EXECUTED);
    CryptoResponse validationResponseTwo =
        new ValidationResponse(CryptoResponse.Status.FAILED, VALIDATION_FAILURES);

    // when && then
    // Aggregation response
    assertEquals(aggregationResponse, aggregationResponseTwo);

    // Validation response
    assertEquals(validationResponse, validationResponseTwo);
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  @ToString(callSuper = true)
  static class AggregationResponse extends CryptoResponse {

    /** Aggregated data. */
    private byte[] data;

    public AggregationResponse(
        @JsonProperty("data") byte[] data,
        @JsonProperty("status") Status status,
        @JsonProperty("message") String message) {
      super(status, message);
      this.data = data;
    }
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  @ToString(callSuper = true)
  static class ValidationResponse extends CryptoResponse {

    /** Validation Failures */
    private Map<String, ValidationFailures> validationFailures;

    public ValidationResponse(
        @JsonProperty("status") Status status,
        @JsonProperty("validationResult") Map<String, ValidationFailures> validationFailures) {
      super(status, null);

      this.validationFailures = validationFailures;
    }
  }
}
