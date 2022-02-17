package com.infoledger.crypto.validation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.api.ValidationFailures;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Response POJO send to parent app (client) from enclave host in response to {@link
 * ValidationRequest}.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ValidationResponse extends CryptoResponse {

  private Map<String, ValidationFailures> validationFailures;

  public ValidationResponse(
      @JsonProperty("status") Status status,
      @JsonProperty("validationResult") Map<String, ValidationFailures> validationFailures) {
    super(status, null);

    this.validationFailures = validationFailures;
  }

  /**
   * Returns per-build OK response.
   *
   * @return OK response with Status=OK.
   */
  public static ValidationResponse ok() {
    return new ValidationResponse(Status.OK, null);
  }

  /**
   * Returns pre-build FAILED response with the failure specified.
   *
   * @param validationFailures Failure message to provide to client.
   * @return FAILED response with failure message.
   */
  public static ValidationResponse failed(Map<String, ValidationFailures> validationFailures) {
    return new ValidationResponse(Status.FAILED, validationFailures);
  }
}
