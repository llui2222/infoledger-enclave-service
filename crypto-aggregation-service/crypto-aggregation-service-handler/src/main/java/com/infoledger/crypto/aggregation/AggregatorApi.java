package com.infoledger.crypto.aggregation;

/**
 * General aggregator interface
 *
 * @param <T> Data type to aggregate
 */
public interface AggregatorApi<T> {

  /**
   * Aggregates new data with existing data and returns aggregation result.
   *
   * @param newData New data to aggregate
   * @param existingData Currently existing data
   * @return Aggregated data
   */
  T aggregate(T newData, T existingData);
}
