package com.infoledger.crypto.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

class CryptoRequestUnitTest {

  private static final String ACCESS_KEY = "access-key-1";
  private static final String SECRET_KEY = "secret-key-1";
  private static final String SESSION_TOKEN = "SessionToken";
  private static final String KMS_KEY_ARN = "arn-1";
  private static final byte[] DATA = {0, 1, 3};
  private static final byte[] EXISTING_DATA = {0, 1};
  private static final byte[] NEW_DATA = {1};

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void testSerDeOfDifferentRequestTypes() throws JsonProcessingException {
    // given
    CryptoRequest aggregationRequest =
        new AggregationRequest(
            KMS_KEY_ARN,
            AggregationRequest.Credentials.fromAwsCredentials(
                new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)),
            NEW_DATA,
            EXISTING_DATA);
    CryptoRequest validationRequest =
        new ValidationRequest(
            KMS_KEY_ARN,
            AggregationRequest.Credentials.fromAwsCredentials(
                new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)),
            DATA);

    // when
    String aggregationRequestJson = mapper.writeValueAsString(aggregationRequest);
    String validationRequestJson = mapper.writeValueAsString(validationRequest);

    CryptoRequest aggregationRequestDeserialized =
        mapper.readValue(aggregationRequestJson, CryptoRequest.class);
    CryptoRequest validationRequestDeserialized =
        mapper.readValue(validationRequestJson, CryptoRequest.class);

    // then
    assertTrue(aggregationRequestDeserialized instanceof AggregationRequest);
    assertTrue(validationRequestDeserialized instanceof ValidationRequest);

    assertEquals(aggregationRequest, aggregationRequestDeserialized);
    assertEquals(validationRequest, validationRequestDeserialized);
  }

  @Test
  void testSerDeOfDifferentRequestTypesSessionAwsCredentials() throws JsonProcessingException {
    // given
    CryptoRequest aggregationRequest =
        new AggregationRequest(
            KMS_KEY_ARN,
            AggregationRequest.Credentials.fromAwsCredentials(
                new BasicSessionCredentials(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN)),
            NEW_DATA,
            EXISTING_DATA);
    CryptoRequest validationRequest =
        new ValidationRequest(
            KMS_KEY_ARN,
            AggregationRequest.Credentials.fromAwsCredentials(
                new BasicSessionCredentials(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN)),
            DATA);

    // when
    String aggregationRequestJson = mapper.writeValueAsString(aggregationRequest);
    String validationRequestJson = mapper.writeValueAsString(validationRequest);

    CryptoRequest aggregationRequestDeserialized =
        mapper.readValue(aggregationRequestJson, CryptoRequest.class);
    CryptoRequest validationRequestDeserialized =
        mapper.readValue(validationRequestJson, CryptoRequest.class);

    // then
    assertTrue(aggregationRequestDeserialized instanceof AggregationRequest);
    assertTrue(validationRequestDeserialized instanceof ValidationRequest);

    assertEquals(aggregationRequest, aggregationRequestDeserialized);
    assertEquals(validationRequest, validationRequestDeserialized);
  }

  @Test
  void testAsAwsCredentialsBasic() {
    // Given
    CryptoRequest.Credentials credentials =
        CryptoRequest.Credentials.fromAwsCredentials(
            new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));

    // When
    AWSCredentials awsCredentials = credentials.asAwsCredentials();

    // Then
    assertTrue(awsCredentials instanceof BasicAWSCredentials);
    assertEquals(ACCESS_KEY, awsCredentials.getAWSAccessKeyId());
    assertEquals(SECRET_KEY, awsCredentials.getAWSSecretKey());
  }

  @Test
  void testAsAwsCredentialsSession() {
    // Given
    CryptoRequest.Credentials credentials =
        CryptoRequest.Credentials.fromAwsCredentials(
            new BasicSessionCredentials(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN));

    // When
    AWSCredentials awsCredentials = credentials.asAwsCredentials();

    // Then
    assertTrue(awsCredentials instanceof BasicSessionCredentials);
    assertEquals(ACCESS_KEY, awsCredentials.getAWSAccessKeyId());
    assertEquals(SECRET_KEY, awsCredentials.getAWSSecretKey());
    assertEquals(SESSION_TOKEN, ((BasicSessionCredentials) awsCredentials).getSessionToken());
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  @ToString(callSuper = true)
  static class AggregationRequest extends CryptoRequest {

    /** Encrypted data to aggregate. */
    private byte[] newData;

    /** Current encrypted aggregated data. */
    private byte[] existingData;

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

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  @ToString(callSuper = true)
  static class ValidationRequest extends CryptoRequest {

    /** Encrypted data to aggregate. */
    private byte[] data;

    public ValidationRequest(
        @JsonProperty("kmsKeyArn") String kmsKeyArn,
        @JsonProperty("credentials") AggregationRequest.Credentials credentials,
        @JsonProperty("data") byte[] data) {
      super(kmsKeyArn, credentials);

      this.data = data;
    }
  }
}
