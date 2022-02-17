package com.infoledger.enclave.service.host.domain.request.aggregation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * InfoLedger aggregation information request
 */
@Getter
public class InfoLedgerAggregationRequest implements Serializable {
  private final String kmsKeyArn;
  private final List<FileS3Info> attachmentFilesS3Infos;
  private final FileS3Info fileResultS3Info;

  @JsonCreator
  public InfoLedgerAggregationRequest(@JsonProperty(value = "kmsKeyArn", required = true) String kmsKeyArn,
                                      @JsonProperty(value = "attachmentFilesS3Infos", required = true) List<FileS3Info> attachmentFilesS3Infos,
                                      @JsonProperty(value = "fileResultS3Info", required = true) FileS3Info fileResultS3Info) {
    this.kmsKeyArn = requireNonNull(kmsKeyArn, "Group Id can not be null.");
    this.attachmentFilesS3Infos = requireNonNull(attachmentFilesS3Infos, "Files S3 infos can not be null.");
    this.fileResultS3Info = requireNonNull(fileResultS3Info, "File S3 info can not be null.");
  }

  @Override
  public String toString() {
    return "{" +
        "\"kmsKeyArn\":\"" + kmsKeyArn + "\"" +
        ", \"attachmentFilesS3Infos\": " + attachmentFilesS3Infos +
        ", \"fileResultS3Info\": " + fileResultS3Info +
        "}";
  }
}
