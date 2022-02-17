package com.infoledger.crypto.aggregation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.crypto.api.CryptoResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Response POJO send to parent app (client) from enclave host in response to {@link
 * AggregationRequest}.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class AggregationResponse extends CryptoResponse {

  private static final String NO_MESSAGE = null;
  private static final byte[] NO_DATA = null;

  /** Result data Nullable in case aggregation fails */
  private final byte[] data;

  public AggregationResponse(
      @JsonProperty("data") byte[] data,
      @JsonProperty("status") Status status,
      @JsonProperty("message") String message) {
    super(status, message);
    this.data = data;
  }

  /**
   * Returns per-build OK response with the actual data content.
   *
   * @param data Aggregated data.
   * @return OK response with Status=OK and aggregated data.
   */
  public static AggregationResponse ok(byte[] data) {
    return new AggregationResponse(data, Status.OK, NO_MESSAGE);
  }

  /**
   * Returns pre-build FAILED response with the failure specified.
   *
   * @param failure Failure message to provide to client.
   * @return FAILED response with failure message.
   */
  public static AggregationResponse failed(String failure) {
    return new AggregationResponse(NO_DATA, Status.FAILED, failure);
  }
}
