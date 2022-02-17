package com.infoledger.crypto.aggregation.api;

import static com.infoledger.crypto.api.CryptoRequest.Credentials.fromAwsCredentials;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AggregationRequestUnitTest {

  private static final String TOKEN = "token-2";
  private static final String ACCESS_KEY_1 = "access-key-1";
  private static final String SECRET_KEY_1 = "secret-key-1";
  private static final String ACCESS_KEY_2 = "access-key-2";
  private static final String SECRET_KEY_2 = "secret-key-2";

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void testRequestSerDeWithDifferentCredentialsTypes() throws JsonProcessingException {
    // given
    final AggregationRequest request1 =
        new AggregationRequest(
            "arn-1",
            fromAwsCredentials(new BasicAWSCredentials(ACCESS_KEY_1, SECRET_KEY_1)),
            new byte[] {1},
            new byte[] {0, 1});
    final AWSCredentials sessionCredentials =
        new BasicSessionCredentials(ACCESS_KEY_2, SECRET_KEY_2, TOKEN);
    final AggregationRequest request2 =
        new AggregationRequest(
            "arn-2", fromAwsCredentials(sessionCredentials), new byte[] {0}, new byte[] {1, 1});

    // when
    final AggregationRequest result1 =
        mapper.readValue(mapper.writeValueAsString(request1), AggregationRequest.class);
    final AggregationRequest result2 =
        mapper.readValue(mapper.writeValueAsString(request2), AggregationRequest.class);

    // then
    assertEquals(request1, result1);
    assertEquals(request2, result2);
  }

  @Test
  void testSessionAndBasicCredentials() {
    // Given
    final AggregationRequest.Credentials credentials1 =
        fromAwsCredentials(new BasicAWSCredentials(ACCESS_KEY_1, SECRET_KEY_1));
    final AWSCredentials credentials =
        new BasicSessionCredentials(ACCESS_KEY_2, SECRET_KEY_2, TOKEN);
    AggregationRequest.Credentials credentials2 = fromAwsCredentials(credentials);

    // When & Then
    assertNull(credentials1.getSessionToken());
    assertNotNull(credentials2.getSessionToken());
  }
}
