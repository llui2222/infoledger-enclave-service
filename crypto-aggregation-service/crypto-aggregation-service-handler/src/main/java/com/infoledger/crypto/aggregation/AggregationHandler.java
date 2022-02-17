package com.infoledger.crypto.aggregation;

import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.aggregation.api.AggregationResponse;
import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.encryption.EncryptionApi;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.crypto.util.Utils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Aggregation operation handler */
public class AggregationHandler
    implements OperationHandler<AggregationRequest, AggregationResponse> {

  private static final String OPERATION_NAME = "aggregation";
  private static final Logger LOG = LogManager.getLogger(AggregationHandler.class);

  @Override
  public AggregationResponse handle(CryptoRequest cryptoRequest, EncryptionApi encryptor) {
    AggregationRequest request = (AggregationRequest) cryptoRequest;
    byte[] newDataDecrypted;
    try {
      newDataDecrypted = encryptor.decrypt(request.getNewData());
      LOG.debug("Successfully decrypted new data");
    } catch (Exception e) {
      LOG.info("Failed to decrypt new data from request : {}", e.getMessage());
      return AggregationResponse.failed(e.getMessage());
    }

    byte[] existingDataDecrypted;
    try {
      existingDataDecrypted =
          Utils.nonEmpty(request.getExistingData())
              ? encryptor.decrypt(request.getExistingData())
              : request.getExistingData();
      LOG.debug("Successfully decrypted new data");
    } catch (Exception e) {
      LOG.info("Failed to decrypt existing data from request : {}", e.getMessage());
      return AggregationResponse.failed(e.getMessage());
    }

    AggregatorApi<byte[]> aggregator;
    try {
      aggregator = AggregatorFactory.forFileType(newDataDecrypted);
    } catch (IOException e) {
      LOG.info("Failed to aggregate data : {}", e.getMessage());
      return AggregationResponse.failed(e.getMessage());
    }

    byte[] aggregated;

    try {
      aggregated = aggregator.aggregate(newDataDecrypted, existingDataDecrypted);
      LOG.debug("Successfully aggregated new and existing data");
    } catch (Exception e) {
      LOG.info("Failed to aggregate data : {}", e.getMessage());
      return AggregationResponse.failed(e.getMessage());
    }

    byte[] aggregatedEncrypted = encryptor.encrypt(aggregated);

    String aggregatedEncryptedAsString = new String(aggregatedEncrypted, StandardCharsets.UTF_8);
    LOG.debug("Encrypted message : {}", aggregatedEncryptedAsString);
    return AggregationResponse.ok(aggregatedEncrypted);
  }

  @Override
  public Class<AggregationRequest> getHandledCryptoRequestType() {
    return AggregationRequest.class;
  }

  @Override
  public String getOperationName() {
    return OPERATION_NAME;
  }
}
