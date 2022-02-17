package com.infoledger.crypto.enclave.app.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.infoledger.crypto.aggregation.AggregationHandler;
import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.aggregation.api.AggregationResponse;
import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.encryption.EncryptionApi;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.crypto.validation.ValidationHandler;
import com.infoledger.crypto.validation.api.ValidationRequest;
import com.infoledger.crypto.validation.api.ValidationResponse;
import java.net.InetAddress;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestsHandlerProviderUnitTest {

  private static final String KMS_KEY_ARN = "kmsKeyArn";
  private static final byte[] NEW_DATA = {1, 2, 3};
  private static final byte[] EXISTING_DATA = {4, 5, 6};

  private final AggregationResponse aggregationResponse = mock(AggregationResponse.class);
  private final ValidationResponse validationResponse = mock(ValidationResponse.class);

  @Test
  void
      testHandlerProviderCanKeepCorrectRequestHandlersAndReturnAppropriateRequestHandlerForAggregationOrValidation() {
    // Given
    AggregationRequest aggregationRequest = prepareAggregationRequest();
    ValidationRequest validationRequest = prepareValidationRequest();
    RequestsHandler requestsHandler =
        RequestsHandlerProvider.get("us-east-1", mock(InetAddress.class), prepareHandlers());

    // When
    CryptoResponse aggregationResponseActual = requestsHandler.handleRequest(aggregationRequest);

    // Then
    assertSame(aggregationResponse, aggregationResponseActual);

    // When
    CryptoResponse validationResponseActual = requestsHandler.handleRequest(validationRequest);

    // Then
    assertSame(validationResponse, validationResponseActual);
  }

  private Map<
          Class<? extends CryptoRequest>,
          OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
      prepareHandlers() {
    AggregationHandler aggregationHandler = mock(AggregationHandler.class);
    when(aggregationHandler.handle(any(AggregationRequest.class), any(EncryptionApi.class)))
        .thenReturn(aggregationResponse);
    ValidationHandler validationHandler = mock(ValidationHandler.class);
    when(validationHandler.handle(any(ValidationRequest.class), any(EncryptionApi.class)))
        .thenReturn(validationResponse);

    return Map.of(
        AggregationRequest.class, aggregationHandler, ValidationRequest.class, validationHandler);
  }

  private AggregationRequest prepareAggregationRequest() {
    CryptoRequest.Credentials credentials = prepareCredentials();

    return new AggregationRequest(KMS_KEY_ARN, credentials, NEW_DATA, EXISTING_DATA);
  }

  private ValidationRequest prepareValidationRequest() {
    CryptoRequest.Credentials credentials = prepareCredentials();

    return new ValidationRequest(KMS_KEY_ARN, credentials, NEW_DATA);
  }

  private CryptoRequest.Credentials prepareCredentials() {
    CryptoRequest.Credentials credentials = new CryptoRequest.Credentials();
    credentials.setAccessKey("accessKey");
    credentials.setSecretKey("secretKey");
    return credentials;
  }
}
