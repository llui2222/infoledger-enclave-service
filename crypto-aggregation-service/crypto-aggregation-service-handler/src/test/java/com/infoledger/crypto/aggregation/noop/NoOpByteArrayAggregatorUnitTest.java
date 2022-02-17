package com.infoledger.crypto.aggregation.noop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NoOpByteArrayAggregatorUnitTest {

  public static final byte[] NEW_DATA = {1, 2, 3};
  public static final byte[] EXISTING_DATA = {5, 6, 7};

  @Test
  void testStubWorksAsExpected() {
    // Given
    NoOpByteArrayAggregator noOpByteArrayAggregator = new NoOpByteArrayAggregator();

    // When && Then
    assertEquals(NEW_DATA, noOpByteArrayAggregator.aggregate(NEW_DATA, EXISTING_DATA));
  }
}
