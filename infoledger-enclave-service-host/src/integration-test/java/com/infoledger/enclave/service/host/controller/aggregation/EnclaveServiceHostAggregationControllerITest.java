package com.infoledger.enclave.service.host.controller.aggregation;

import com.infoledger.enclave.service.host.InfoLedgerEnclaveServiceHostApplication;
import com.infoledger.enclave.service.host.TestApplicationContextInitializer;
import com.infoledger.enclave.service.host.config.StubAwsCognitoIdTokenProcessor;
import com.infoledger.enclave.service.host.config.TestConfig;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.request.aggregation.InfoLedgerAggregationRequest;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
import com.jayway.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link EnclaveServiceHostAggregationController}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@Import(TestConfig.class)
@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = InfoLedgerEnclaveServiceHostApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:integration-test.yml"})
@Slf4j
class EnclaveServiceHostAggregationControllerITest {

  @Value("${amazon.aws.bucket.attachments:}")
  private String awsBucketForAttachments;

  @Value("${amazon.aws.bucket.aggregations:}")
  private String awsBucketForAggregations;

  @LocalServerPort
  private int port;

  @Autowired
  private AwsS3FileService awsS3FileService;

  @Test
  void testSuccessfulAggregation() throws IOException {
    InfoLedgerAggregationRequest infoLedgerAggregationRequest = createInfoLedgerAggregationRequest();
    String requestBody = infoLedgerAggregationRequest.toString();
    Response res = given()
        .port(port)
        .baseUri(baseURI)
        .header("Authorization", "Bearer=" + StubAwsCognitoIdTokenProcessor.TEST_BEARER, "Content-Type", "application/json")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .post("v1/aggregate");
    assertThat(res.getStatusCode()).isEqualTo(200);
  }

  private InfoLedgerAggregationRequest createInfoLedgerAggregationRequest() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    FileS3Info fileForAggregationInfoResult = createFileS3Info("1617904825262998/aggregation",
        awsBucketForAggregations,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/aggregationResult.xls"))
            .getFile()).toPath()));
    FileS3Info fileForAggregationInfoFirst = createFileS3Info("1617904825262998/attachmentOne",
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentOne.xls"))
            .getFile()).toPath()));
    FileS3Info fileForAggregationInfoSecond = createFileS3Info("1617904825262998/attachmentTwo",
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentTwo.xls"))
            .getFile()).toPath()));
    FileS3Info fileForAggregationInfoThird = createFileS3Info("1617904825262998/attachmentThree",
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentThree.xls"))
            .getFile()).toPath()));
    return new InfoLedgerAggregationRequest("kmsKeyArn",
        Arrays.asList(fileForAggregationInfoFirst,
            fileForAggregationInfoSecond,
            fileForAggregationInfoThird),
        fileForAggregationInfoResult);
  }

  private FileS3Info createFileS3Info(String fileKey, String bucketName, byte[] fileByteArray) {
    FileS3Info fileForAggregationInfo = new FileS3Info(fileKey, bucketName);
    fileForAggregationInfo = awsS3FileService.storeFileProcessingResult(fileForAggregationInfo, fileByteArray);
    return fileForAggregationInfo;
  }
}
