package com.infoledger.enclave.service.host.domain.request.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * InfoLedger validation information request
 */
@Getter
public class InfoLedgerValidationRequest implements Serializable {
  private final String kmsKeyArn;
  private final List<FileS3Info> attachmentFilesS3Infos;

  @JsonCreator
  public InfoLedgerValidationRequest(@JsonProperty(value = "kmsKeyArn", required = true) String kmsKeyArn,
                                     @JsonProperty(value = "attachmentFilesS3Infos", required = true) List<FileS3Info> attachmentFilesS3Infos) {
    this.kmsKeyArn = requireNonNull(kmsKeyArn, "Group Id can not be null.");
    this.attachmentFilesS3Infos = requireNonNull(attachmentFilesS3Infos, "Files S3 infos can not be null.");
  }

  @Override
  public String toString() {
    return "{" +
        "\"kmsKeyArn\":\"" + kmsKeyArn + "\"" +
        ", \"attachmentFilesS3Infos\": " + attachmentFilesS3Infos +
        "}";
  }
}
