package com.infoledger.crypto.aggregation;

import static com.infoledger.crypto.aggregation.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.aggregation.api.AggregationResponse;
import com.infoledger.crypto.encryption.EncryptionApi;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class AggregationHandlerUnitTest {

  private final AggregationRequest request = mock(AggregationRequest.class);
  private final EncryptionApi encryptor = mock(EncryptionApi.class);

  private final AggregationHandler handler = new AggregationHandler();

  @Test
  void givenOnlyNewDataWhenCallHandleThenSuccessfullyHandled()
      throws IOException, URISyntaxException {
    // Given
    final byte[] csvData = loadResource("csv-1-10-rows.csv");
    when(request.getNewData()).thenReturn(csvData);
    when(encryptor.decrypt(csvData)).thenReturn(csvData);
    when(encryptor.encrypt(csvData)).thenReturn(csvData);

    // When
    AggregationResponse aggregationResponse = handler.handle(request, encryptor);

    // Then
    assertTrue(aggregationResponse.isOk());
  }
}
