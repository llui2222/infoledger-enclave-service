package com.infoledger.enclave.service.host.domain.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class FileProcessingFailureReason {
    private final String fileName;
    private final String failureReason;

    public FileProcessingFailureReason(@JsonProperty(value = "fileName") String fileName,
                                       @JsonProperty(value = "failureReason") String failureReason) {
        this.fileName = fileName;
        this.failureReason = failureReason;
    }
}
