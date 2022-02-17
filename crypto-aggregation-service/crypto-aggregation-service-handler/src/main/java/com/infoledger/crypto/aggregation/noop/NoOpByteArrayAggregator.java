package com.infoledger.crypto.aggregation.noop;

import com.infoledger.crypto.aggregation.AggregatorApi;

/** Stub implementation for testing purposes only. Does nothing and returns new data. */
public class NoOpByteArrayAggregator implements AggregatorApi<byte[]> {

  @Override
  public byte[] aggregate(byte[] newData, byte[] existingData) {
    return newData;
  }
}
