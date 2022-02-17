package com.infoledger.crypto.enclave.app.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.infoledger.crypto.aggregation.AggregationHandler;
import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.crypto.validation.api.ValidationRequest;
import java.net.InetAddress;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestsHandlerUnitTest {

  private static final String KMS_KEY_ARN = "kmsKeyArn";
  private static final byte[] NEW_DATA = {1, 2, 3};

  @Test
  void testRequestHandledFailsIfNoRequestHandlerExistsForProvidedRequestType() {
    // Given
    ValidationRequest validationRequest = prepareValidationRequest();
    RequestsHandler requestsHandler =
        new RequestsHandler("us-east-1", mock(InetAddress.class), prepareAggregationHandler());

    // When && Then
    assertThrows(
        EnclaveClientException.class, () -> requestsHandler.handleRequest(validationRequest));
  }

  private Map<
          Class<? extends CryptoRequest>,
          OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
      prepareAggregationHandler() {
    AggregationHandler aggregationHandler = mock(AggregationHandler.class);

    return Map.of(AggregationRequest.class, aggregationHandler);
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
