package com.infoledger.crypto.aggregation.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.infoledger.crypto.api.CryptoResponse;
import org.junit.jupiter.api.Test;

class AggregationResponseUnitTest {

  private static final byte[] DATA = {1, 2, 3};
  private static final String MESSAGE = "Message";

  @Test
  void givenFullyPopulatedResponseWhenCallGettersThenReturnExpectedValues() {
    // Given
    AggregationResponse aggregationResponse =
        new AggregationResponse(DATA, CryptoResponse.Status.OK, MESSAGE);

    // When && Then
    assertEquals(DATA, aggregationResponse.getData());
    assertEquals(MESSAGE, aggregationResponse.getMessage());
    assertEquals(CryptoResponse.Status.OK, aggregationResponse.getStatus());
  }

  @Test
  void givenTwoFullyPopulatedResponsesWithSameDataWhenCallEqualsThenReturnTrue() {
    // Given
    AggregationResponse aggregationResponseOne =
        new AggregationResponse(DATA, CryptoResponse.Status.OK, MESSAGE);
    AggregationResponse aggregationResponseTwo =
        new AggregationResponse(DATA, CryptoResponse.Status.OK, MESSAGE);

    // When && Then
    assertEquals(aggregationResponseOne, aggregationResponseTwo);
  }

  @Test
  void givenAggregationResponseWhenCallMethodOkThenReturnExpectedResponse() {
    // Given
    AggregationResponse aggregationResponse = AggregationResponse.ok(DATA);

    // When && Then
    assertEquals(DATA, aggregationResponse.getData());
    assertNull(aggregationResponse.getMessage());
    assertEquals(CryptoResponse.Status.OK, aggregationResponse.getStatus());
  }

  @Test
  void givenAggregationResponseWhenCallMethodFailedThenReturnExpectedResponse() {
    // Given
    AggregationResponse aggregationResponse = AggregationResponse.failed(MESSAGE);

    // When && Then
    assertEquals(MESSAGE, aggregationResponse.getMessage());
    assertNull(aggregationResponse.getData());
    assertEquals(CryptoResponse.Status.FAILED, aggregationResponse.getStatus());
  }
}
