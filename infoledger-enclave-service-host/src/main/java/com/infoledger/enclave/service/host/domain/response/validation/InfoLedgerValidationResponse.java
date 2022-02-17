package com.infoledger.enclave.service.host.domain.response.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class InfoLedgerValidationResponse {

  private final Status status;
  private final Map<String, ValidationResponse> validationResultsPerFile;
  private final List<FileProcessingFailureReason> validationFailures;

  public InfoLedgerValidationResponse(
      @JsonProperty("status") Status status,
      @JsonProperty("validationResultsPerFile") Map<String, ValidationResponse> validationResultsPerFile,
      @JsonProperty("validationFailures") List<FileProcessingFailureReason> validationFailures) {
    this.status = status;
    this.validationResultsPerFile = validationResultsPerFile;
    this.validationFailures = validationFailures;
  }

  /**
   * Returns pre-build OK response.
   *
   * @return OK response.
   */
  public static InfoLedgerValidationResponse ok(Map<String, ValidationResponse> validationResultsPerFile) {
    return new InfoLedgerValidationResponse(Status.OK, validationResultsPerFile, null);
  }

  /**
   * Returns pre-build FAILED response with the failures specified.
   *
   * @param validationResultsPerFile Map with file name as a key  and validation response.
   * @param validationFailures List of files and validation Failure messages to provide to client.
   * @return FAILED response with failure message.
   */
  public static InfoLedgerValidationResponse failed(Map<String, ValidationResponse> validationResultsPerFile,
                                                    List<FileProcessingFailureReason> validationFailures) {
    return new InfoLedgerValidationResponse(Status.FAILED, validationResultsPerFile, validationFailures);
  }
}

