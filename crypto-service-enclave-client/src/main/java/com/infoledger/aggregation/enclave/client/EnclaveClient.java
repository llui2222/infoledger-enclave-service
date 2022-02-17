package com.infoledger.aggregation.enclave.client;

import static com.infoledger.crypto.api.CryptoRequest.Credentials.fromAwsCredentials;
import static com.infoledger.crypto.util.DivideForChunksUtil.CHUNK_SIZE;
import static com.infoledger.crypto.util.DivideForChunksUtil.splitIntoChunks;

import com.amazonaws.auth.AWSCredentials;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoledger.crypto.aggregation.api.AggregationRequest;
import com.infoledger.crypto.aggregation.api.AggregationResponse;
import com.infoledger.crypto.validation.api.ValidationRequest;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.vsockj.VSock;
import com.infoledger.vsockj.VSockAddress;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Client responsible for communication between parent application and enclave application. */
public class EnclaveClient {

  private static final ObjectMapper MAPPER = buildMapper();
  private static final Logger LOG = LogManager.getLogger(EnclaveClient.class);

  /**
   * Enclave CID. Can be found via
   *
   * <pre>
   *     nitro-cli describe-enclaves | jq -r ".[0].EnclaveCID"
   * </pre>
   *
   * <p>command-line call.
   */
  private final int enclaveCid;

  /** Port on which enclave application is started. */
  private final int enclavePort;

  public EnclaveClient(int enclaveCid, int enclavePort) {
    this.enclaveCid = enclaveCid;
    this.enclavePort = enclavePort;
  }

  /**
   * API to enable data aggregation in csv format - 2 byte array sources to be aggregated by anclave
   * application.
   *
   * <p>Sources are encrypted with `kmsKeyArn` and can be decrypted with this key as well.
   *
   * @param credentials AWS credentials to use when encrypting/decrypting data
   * @param kmsKeyArn MKS Key ARN with permission to encrypt/decrypt data.
   * @param newData New data to aggregate with existing source
   * @param currentData Current aggregated data to append new data to
   * @return Aggregated and encrypted data
   * @throws EnclaveAggregationException Exception thrown in case the aggregation fails.
   */
  public byte[] aggregate(
      AWSCredentials credentials, String kmsKeyArn, byte[] newData, @Nullable byte[] currentData) {
    LOG.debug("Aggregating data with key {}", kmsKeyArn);

    try (VSock client = new VSock(new VSockAddress(enclaveCid, enclavePort))) {
      AggregationRequest request =
          new AggregationRequest(kmsKeyArn, fromAwsCredentials(credentials), newData, currentData);
      byte[] dataToSend = MAPPER.writeValueAsBytes(request);

      for (byte[] chunk : splitIntoChunks(dataToSend, CHUNK_SIZE)) {
        client.getOutputStream().write(chunk);
      }
      AggregationResponse response =
          MAPPER.readValue(client.getInputStream(), AggregationResponse.class);

      if (response.isOk()) {
        LOG.debug("Successfully aggregated data");
        return response.getData();
      } else {
        throw new EnclaveAggregationException(response.getMessage());
      }
    } catch (Exception e) {
      LOG.info("Failed to aggregate data : ", e);
      throw new EnclaveAggregationException("Failed to aggregate data: " + e.getMessage(), e);
    }
  }

  public ValidationResponse validate(AWSCredentials credentials, String kmsKeyArn, byte[] data) {
    LOG.debug("Validating data with key {}", kmsKeyArn);

    try (VSock client = new VSock(new VSockAddress(enclaveCid, enclavePort))) {
      ValidationRequest request =
          new ValidationRequest(kmsKeyArn, fromAwsCredentials(credentials), data);
      byte[] dataToSend = MAPPER.writeValueAsBytes(request);

      for (byte[] chunk : splitIntoChunks(dataToSend, CHUNK_SIZE)) {
        client.getOutputStream().write(chunk);
      }

      return MAPPER.readValue(client.getInputStream(), ValidationResponse.class);
    } catch (Exception e) {
      LOG.error("Failed to validate data : ", e);
      throw new EnclaveValidationException("Failed to validate data: " + e.getMessage(), e);
    }
  }

  /**
   * Builds custom json serde used to serialize and deserialize request and response to parent
   * application.
   *
   * @return JSON serializer.
   */
  private static ObjectMapper buildMapper() {
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
