package com.infoledger.crypto.enclave.app.controller;

import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.enclave.app.EnclaveApplication;
import com.infoledger.crypto.encryption.EncryptionApi;
import com.infoledger.crypto.encryption.EncryptionFactory;
import com.infoledger.crypto.handler.OperationHandler;
import java.net.InetAddress;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Top-level service responsible for handling incoming requests.
 *
 * <p>Request handling logic moved out of main application to separate responsibilies as {@link
 * EnclaveApplication} is mainly responsible for setting up enclave application and launching server
 * and proxy.
 *
 * <p>This handler aim to separate business logic form infrastructure setup.
 */
public class RequestsHandler {

  private static final Logger LOG = LogManager.getLogger(RequestsHandler.class);

  private final String region;
  private final InetAddress localServerAddress;

  /** Registry of operation handlers. This handlers are pluggable via a dedicated modules. */
  private final Map<
          Class<? extends CryptoRequest>,
          OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
      handlers;

  public RequestsHandler(
      String region,
      InetAddress localServerAddress,
      Map<
              Class<? extends CryptoRequest>,
              OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
          handlers) {
    this.region = region;
    this.localServerAddress = localServerAddress;
    this.handlers = handlers;
  }

  public CryptoResponse handleRequest(CryptoRequest request) {
    LOG.debug("Received crypto request : {}", request.getClass().getSimpleName());

    EncryptionApi encryptor =
        EncryptionFactory.getEncryptor(
            region,
            request.getCredentials().asAwsCredentials(),
            localServerAddress,
            request.getKmsKeyArn());

    LOG.debug("Delegating to operation handler");
    OperationHandler<? extends CryptoRequest, ? extends CryptoResponse> handler =
        handlers.get(request.getClass());

    if (handler == null) {
      LOG.error("No handler available for request type: {}", request.getClass().getSimpleName());
      throw new EnclaveClientException("No handler available for request type");
    }

    return handler.handle(request, encryptor);
  }
}
