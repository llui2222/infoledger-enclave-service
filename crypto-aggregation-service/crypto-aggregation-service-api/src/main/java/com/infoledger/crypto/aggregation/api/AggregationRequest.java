package com.infoledger.crypto.aggregation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.crypto.api.CryptoRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Request POJO. */
@Getter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AggregationRequest extends CryptoRequest {

  /** Encrypted data to aggregate. */
  private final byte[] newData;

  /** Existing encrypted aggregated data. */
  private final byte[] existingData;

  public AggregationRequest(
      @JsonProperty("kmsKeyArn") String kmsKeyArn,
      @JsonProperty("credentials") Credentials credentials,
      @JsonProperty("newData") byte[] newData,
      @JsonProperty("existingData") byte[] existingData) {
    super(kmsKeyArn, credentials);

    this.newData = newData;
    this.existingData = existingData;
  }
}
