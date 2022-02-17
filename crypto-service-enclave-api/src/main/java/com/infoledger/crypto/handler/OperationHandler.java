package com.infoledger.crypto.handler;

import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.encryption.EncryptionApi;

/**
 * Common interface for all operation handlers.
 *
 * @param <S> Source data type
 * @param <T> Result data type - crypto response.
 */
public interface OperationHandler<S extends CryptoRequest, T extends CryptoResponse> {

  /**
   * Handle Operation.
   *
   * @param request Request data.
   * @param encryptor Data encryptor
   * @return Operation result data
   */
  T handle(CryptoRequest request, EncryptionApi encryptor);

  /**
   * Gets the crypto request type this handler is able to handle.
   *
   * @return {@link CryptoRequest} type.
   */
  Class<S> getHandledCryptoRequestType();

  /**
   * Gets operation name this service is able to handle, i.e. validation, aggregation.
   *
   * @return Operation name - validation, aggregation.
   */
  String getOperationName();
}
