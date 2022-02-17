package com.infoledger.crypto.enclave.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.aggregation.api.AggregationResponse;
import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.crypto.validation.api.ValidationRequest;
import com.infoledger.crypto.validation.api.ValidationResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;

class EnclaveApplicationUnitTest {

  private static final String VALIDATION = "validation";
  private static final String REGION_PARAMETER = "--region";
  private static final String US_EAST_1 = "us-east-1";
  private static final String ENCLAVE_PORT_PARAMETER = "--enclave-port";
  private static final String ENCLAVE_PORT = "5000";
  private static final String PROXY_PORT_PARAMETER = "--proxy-port";
  private static final String PROXY_PORT = "8443";
  private static final String OPERATIONS_PARAMETER = "--operations";
  private static final String AGGREGATION = "aggregation";

  @Test
  void mapperSerDe() throws JsonProcessingException {
    // given
    AggregationResponse response = AggregationResponse.ok(new byte[] {1, 0, 1});
    ObjectMapper mapper = EnclaveApplication.buildMapper();

    // when
    AggregationResponse result =
        mapper.readValue(mapper.writeValueAsString(response), AggregationResponse.class);

    // then
    assertEquals(response.getMessage(), result.getMessage());
    assertEquals(response.getStatus(), result.getStatus());
    assertArrayEquals(response.getData(), result.getData());
  }

  @Test
  void mapperValidationSerDe() throws JsonProcessingException {
    // given
    ValidationResponse response = ValidationResponse.ok();
    ObjectMapper mapper = EnclaveApplication.buildMapper();

    // when
    ValidationResponse result =
        mapper.readValue(mapper.writeValueAsString(response), ValidationResponse.class);

    // then
    assertEquals(response.getMessage(), result.getMessage());
    assertEquals(response.getStatus(), result.getStatus());
  }

  @Test
  void testHandlersParsing() {
    // given
    String[] args =
        new String[] {
          REGION_PARAMETER,
          US_EAST_1,
          ENCLAVE_PORT_PARAMETER,
          ENCLAVE_PORT,
          PROXY_PORT_PARAMETER,
          PROXY_PORT,
          OPERATIONS_PARAMETER,
          AGGREGATION,
          VALIDATION
        };

    // when
    Map<
            Class<? extends CryptoRequest>,
            OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
        handlers = EnclaveApplication.resolveHandlers(EnclaveApplication.parseArgs(args));

    // then
    assertEquals(2, handlers.size());
    assertTrue(handlers.containsKey(AggregationRequest.class));
    assertTrue(handlers.containsKey(ValidationRequest.class));
  }

  @Test
  void testSingleOperationArgumentParsing() {
    // given
    String[] args =
        new String[] {
          REGION_PARAMETER,
          US_EAST_1,
          ENCLAVE_PORT_PARAMETER,
          ENCLAVE_PORT,
          PROXY_PORT_PARAMETER,
          PROXY_PORT,
          OPERATIONS_PARAMETER,
          VALIDATION
        };

    // when
    CommandLine cmd = EnclaveApplication.parseArgs(args);
    Map<
            Class<? extends CryptoRequest>,
            OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
        handlers = EnclaveApplication.resolveHandlers(cmd);

    // then
    assertEquals(1, handlers.size());
    assertTrue(handlers.containsKey(ValidationRequest.class));
    assertFalse(cmd.hasOption("reattempts-number"));
  }

  @Test
  void testSingleOperationArgumentWithReattemptsNumberParsing() {
    // given
    String[] args =
        new String[] {
          REGION_PARAMETER,
          US_EAST_1,
          ENCLAVE_PORT_PARAMETER,
          ENCLAVE_PORT,
          PROXY_PORT_PARAMETER,
          PROXY_PORT,
          OPERATIONS_PARAMETER,
          VALIDATION,
          "--reattempts-number",
          "4"
        };

    // when
    CommandLine cmd = EnclaveApplication.parseArgs(args);
    Map<
            Class<? extends CryptoRequest>,
            OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
        handlers = EnclaveApplication.resolveHandlers(cmd);

    // then
    assertEquals(1, handlers.size());
    assertTrue(handlers.containsKey(ValidationRequest.class));
    assertEquals("4", cmd.getOptionValue("reattempts-number"));
  }

  @Test
  @SuppressWarnings("rawtypes")
  void testAvailableOperationHandlers() {
    // when
    Iterator<OperationHandler> handlersIterator =
        ServiceLoader.load(OperationHandler.class).iterator();

    // then
    assertNotNull(handlersIterator);
    assertEquals(AGGREGATION, handlersIterator.next().getOperationName());
    assertEquals(VALIDATION, handlersIterator.next().getOperationName());
  }
}
