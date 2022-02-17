package com.infoledger.crypto.validation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.crypto.api.CryptoRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/** Request for data validation */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ValidationRequest extends CryptoRequest {

  /** Encrypted data to aggregate. */
  private byte[] data;

  public ValidationRequest(
      @JsonProperty("kmsKeyArn") String kmsKeyArn,
      @JsonProperty("credentials") Credentials credentials,
      @JsonProperty("data") byte[] data) {
    super(kmsKeyArn, credentials);

    this.data = data;
  }
}
