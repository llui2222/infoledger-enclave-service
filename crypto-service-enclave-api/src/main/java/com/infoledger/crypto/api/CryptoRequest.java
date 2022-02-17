package com.infoledger.crypto.api;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Common request POJO base. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "clazz")
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
public abstract class CryptoRequest {

  /** KMS Key ARN used to encrypt/decrypt data. Enclave app must be granted permission to use it. */
  private final String kmsKeyArn;

  /** AWS credentials. */
  private final Credentials credentials;

  protected CryptoRequest(
      @JsonProperty("kmsKeyArn") String kmsKeyArn,
      @JsonProperty("credentials") Credentials credentials) {
    this.kmsKeyArn = kmsKeyArn;
    this.credentials = credentials;
  }

  /**
   * {@link AWSCredentials} implementations are not jackson deserializable as do not contain default
   * constructor thus need to provide a custom one.
   */
  @Data
  public static class Credentials {
    private String accessKey;
    private String secretKey;
    private String sessionToken;

    public Credentials() {
      // empty ctor.
    }

    public static Credentials fromAwsCredentials(AWSCredentials credentials) {
      if (credentials instanceof AWSSessionCredentials) {
        return new Credentials()
            .setAccessKey(credentials.getAWSAccessKeyId())
            .setSecretKey(credentials.getAWSSecretKey())
            .setSessionToken(((AWSSessionCredentials) credentials).getSessionToken());
      }
      return new Credentials()
          .setAccessKey(credentials.getAWSAccessKeyId())
          .setSecretKey(credentials.getAWSSecretKey());
    }

    public AWSCredentials asAwsCredentials() {
      return sessionToken != null
          ? new BasicSessionCredentials(accessKey, secretKey, sessionToken)
          : new BasicAWSCredentials(accessKey, secretKey);
    }
  }
}
