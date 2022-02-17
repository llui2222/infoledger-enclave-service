package com.infoledger.enclave.service.host.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * InfoLedger file S3 information
 */
@Getter
public class FileS3Info implements Serializable {
    private final String fileKey;
    private final String bucketName;

    @JsonCreator
    public FileS3Info(@JsonProperty(value = "fileKey", required = true) String fileKey,
                      @JsonProperty(value = "bucketName", required = true) String bucketName) {
        this.fileKey = requireNonNull(fileKey, "File key can not be null.");
        this.bucketName = requireNonNull(bucketName, "Bucket name can not be null.");
    }

    @Override
    public String toString() {
        return "{\"fileKey\":\"" + fileKey + "\"" +
                ", \"bucketName\":\"" + bucketName + "\"" +
                "}";
    }
}
