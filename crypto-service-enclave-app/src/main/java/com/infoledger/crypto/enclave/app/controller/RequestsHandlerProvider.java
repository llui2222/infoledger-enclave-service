package com.infoledger.crypto.enclave.app.controller;

import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.handler.OperationHandler;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/** {@link RequestsHandler} registry. */
public class RequestsHandlerProvider {

  private static final Map<String, RequestsHandler> registry = new HashMap<>();

  private RequestsHandlerProvider() {
    // No logic needed here
  }

  public static RequestsHandler get(
      String region,
      InetAddress localServerAddress,
      Map<
              Class<? extends CryptoRequest>,
              OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
          operationHandlers) {
    return registry.computeIfAbsent(
        region, r -> new RequestsHandler(r, localServerAddress, operationHandlers));
  }
}
