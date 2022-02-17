package com.infoledger.crypto.enclave.app;

import static com.infoledger.crypto.util.DivideForChunksUtil.CHUNK_SIZE;
import static com.infoledger.crypto.util.DivideForChunksUtil.splitIntoChunks;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.enclave.app.controller.RequestsHandler;
import com.infoledger.crypto.enclave.app.controller.RequestsHandlerProvider;
import com.infoledger.crypto.enclave.app.server.EnclaveServer;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.vsockj.VSock;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point to the application.
 *
 * <p>Need to provide the target region (defines the target KMS API to communicate with), port on
 * which to start the application, proxy port - port on which the vsock-proxy has been started and
 * operations - which services should be loaded on start up(aggregation, validation or both). Also
 * number of reconnect attempts could be specified(optional, default value is 5).
 *
 * <p>Example usage: <code>
 * java -cp /app/resources:/app/classes:/app/libs/* com.infoledger.aggregation.enclave.app.EnclaveApplication
 * --region us-east-1
 * --enclave-port 5000
 * --proxy-port 8443
 * --operations validation aggregation
 * --reattempts-number 5
 * </code> These 4 settings - region, enclave port, proxy port and operations - should be provided
 * once building Docker image. Also could be provided optional parameter reattempts number.
 * `Dockerfile` contains:
 *
 * <pre>
 *    ENV AWS_REGION ENCLAVE_REGION
 *    ENV APP_PORT ENCLAVE_PORT
 *    ENV VSOCK_PORT PROXY_PORT
 *    ENV OPERATIONS ENCLAVE_OPERATIONS
 *    ENV REATTEMPTS_NUMBER NUMBER_OF_CONNECT_ATTEMPTS
 * </pre>
 *
 * <p>section which sets environment variables. So before building docker image, `ENCLAVE_REGION`,
 * `ENCLAVE_PORT`, `PROXY_PORT` and 'OPERATIONS' should be specified. environment variable
 * 'REATTEMPTS_NUMBER' is optional, default value is '5'
 */
public class EnclaveApplication {

  private static final Logger LOG = LogManager.getLogger(EnclaveApplication.class);

  private static final String OPERATIONS_PARAMETER_NAME = "operations";
  private static final String REGION_PARAMETER_NAME = "region";
  private static final String ENCLAVE_PORT_PARAMETER_NAME = "enclave-port";
  private static final String PROXY_PORT_PARAMETER_NAME = "proxy-port";
  private static final String REATTEMPTS_NUMBER_PARAMETER_NAME = "reattempts-number";

  private static final int DEFAULT_REATTEMPTS_NUMBER = 5;

  private static final ObjectMapper MAPPER = buildMapper();

  public static void main(String[] args) throws IOException {
    CommandLine cmd = parseArgs(args);

    String region = cmd.getOptionValue(REGION_PARAMETER_NAME);
    int enclavePort = Integer.parseInt(cmd.getOptionValue(ENCLAVE_PORT_PARAMETER_NAME));
    int proxyPort = Integer.parseInt(cmd.getOptionValue(PROXY_PORT_PARAMETER_NAME));
    int reattemptsNumber =
        cmd.hasOption(REATTEMPTS_NUMBER_PARAMETER_NAME)
            ? Integer.parseInt(cmd.getOptionValue(REATTEMPTS_NUMBER_PARAMETER_NAME))
            : DEFAULT_REATTEMPTS_NUMBER;

    Map<
            Class<? extends CryptoRequest>,
            OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
        handlers = resolveHandlers(cmd);

    LOG.info("Region: {}", region);
    LOG.info("Enclave Port: {}", enclavePort);
    LOG.info("Proxy Port: {}", proxyPort);
    LOG.info("Operations: {}", (Object[]) cmd.getOptionValues(OPERATIONS_PARAMETER_NAME));
    LOG.info("Handlers: {}", handlers);
    LOG.info("Connection reattempts number: {}", reattemptsNumber);

    InetAddress loopbackAddress = InetAddress.getLoopbackAddress();

    try (EnclaveServer server = new EnclaveServer(loopbackAddress, enclavePort, reattemptsNumber)) {

      server.runServer(
          peerVSock -> {
            try {
              handleVsockRequest(region, peerVSock, server, handlers);
            } catch (IOException e) {
              LOG.warn(e.getMessage(), e);
            }
          });

      server.runProxyServer(proxyPort);
    }
  }

  static CommandLine parseArgs(String[] args) {
    Options options = new Options();

    options.addOption(
        Option.builder("r")
            .longOpt(REGION_PARAMETER_NAME)
            .hasArg(true)
            .desc("Target KMS API region")
            .required(true)
            .build());

    options.addOption(
        Option.builder("ep")
            .longOpt(ENCLAVE_PORT_PARAMETER_NAME)
            .hasArg(true)
            .desc("Enclave Application Port")
            .required(true)
            .build());

    options.addOption(
        Option.builder("pp")
            .longOpt(PROXY_PORT_PARAMETER_NAME)
            .hasArg(true)
            .desc("Port on which vsock-proxy has been started")
            .required(true)
            .build());

    options.addOption(
        Option.builder("ops")
            .longOpt(OPERATIONS_PARAMETER_NAME)
            .hasArgs()
            .desc(
                "List of operation handlers to enable on this enclave instance. "
                    + "Currently supported values are aggregation, validation")
            .required(true)
            .build());

    options.addOption(
        Option.builder("rn")
            .longOpt(REATTEMPTS_NUMBER_PARAMETER_NAME)
            .hasArg(true)
            .desc("Number of reconnect attempts")
            .required(false)
            .build());

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    try {
      return parser.parse(options, args);
    } catch (ParseException e) {
      LOG.error(e.getMessage());
      formatter.printHelp("utility-name", options);

      System.exit(1);
      return null;
    }
  }

  static Map<
          Class<? extends CryptoRequest>,
          OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
      resolveHandlers(CommandLine cmd) {
    List<String> operations = Arrays.asList(cmd.getOptionValues(OPERATIONS_PARAMETER_NAME));
    Map<
            Class<? extends CryptoRequest>,
            OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
        handlers = new HashMap<>();

    ServiceLoader.load(OperationHandler.class)
        .forEach(
            handler -> {
              String operation = handler.getOperationName();
              if (containsIgnoringCase(operations, operation)) {
                handlers.put(handler.getHandledCryptoRequestType(), handler);
              }
            });

    return handlers;
  }

  private static void handleVsockRequest(
      String region,
      VSock peerVSock,
      EnclaveServer enclaveServer,
      Map<
              Class<? extends CryptoRequest>,
              OperationHandler<? extends CryptoRequest, ? extends CryptoResponse>>
          handlers)
      throws IOException {
    CryptoRequest request = MAPPER.readValue(peerVSock.getInputStream(), CryptoRequest.class);

    try {
      RequestsHandler handler =
          RequestsHandlerProvider.get(region, enclaveServer.getLocalServerAddress(), handlers);
      CryptoResponse response = handler.handleRequest(request);
      LOG.info("Writing the response back to host");

      byte[] responseBytes = MAPPER.writeValueAsBytes(response);

      for (byte[] chunk : splitIntoChunks(responseBytes, CHUNK_SIZE)) {
        peerVSock.getOutputStream().write(chunk);
      }
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
      peerVSock
          .getOutputStream()
          .write(
              MAPPER.writeValueAsBytes(
                  e.getMessage() + MAPPER.writeValueAsString(e.getStackTrace())));
    }
  }

  private static boolean containsIgnoringCase(List<String> source, String target) {
    return source.contains(target) || source.contains(target.toLowerCase(Locale.ROOT));
  }

  /**
   * Builds custom json serde used to serialize and deserialize request and response to parent
   * application.
   *
   * @return JSON serializer.
   */
  static ObjectMapper buildMapper() {
    JsonFactory jsonFactory = new JsonFactory();

    // need to disable stream closing after read as we will write into the same stream an answer
    // see https://github.com/FasterXML/jackson-databind/issues/697
    jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }
}
