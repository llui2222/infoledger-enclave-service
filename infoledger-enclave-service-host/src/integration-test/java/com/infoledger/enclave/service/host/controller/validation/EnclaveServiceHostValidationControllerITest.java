package com.infoledger.enclave.service.host.controller.validation;

import com.infoledger.enclave.service.host.InfoLedgerEnclaveServiceHostApplication;
import com.infoledger.enclave.service.host.TestApplicationContextInitializer;
import com.infoledger.enclave.service.host.config.StubAwsCognitoIdTokenProcessor;
import com.infoledger.enclave.service.host.config.TestConfig;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.request.validation.InfoLedgerValidationRequest;
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
import java.util.Collections;
import java.util.Objects;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link EnclaveServiceHostValidationController}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@Import(TestConfig.class)
@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = InfoLedgerEnclaveServiceHostApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:integration-test.yml"})
@Slf4j
class EnclaveServiceHostValidationControllerITest {
  private static final String FILE_KEY_VALIDATION_RESULT_ONE = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.one";
  private static final String FILE_KEY_VALIDATION_RESULT_TWO = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.two";
  private static final String FILE_KEY_VALIDATION_RESULT_THREE = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.three";

  @Value("${amazon.aws.bucket.attachments:}")
  private String awsBucketForAttachments;

  @LocalServerPort
  private int port;

  @Autowired
  private AwsS3FileService awsS3FileService;

  @Test
  void testSuccessfulValidation() throws IOException {
    InfoLedgerValidationRequest infoLedgerValidationRequest = createInfoLedgerValidationRequest();
    String requestBody = infoLedgerValidationRequest.toString();
    Response res = given()
        .port(port)
        .baseUri(baseURI)
        .header("Authorization", "Bearer=" + StubAwsCognitoIdTokenProcessor.TEST_BEARER, "Content-Type", "application/json")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .post("v1/validate");
    assertThat(res.getStatusCode()).isEqualTo(200);
  }

  private InfoLedgerValidationRequest createInfoLedgerValidationRequest() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    FileS3Info fileForValidationInfoOne = createFileS3Info(FILE_KEY_VALIDATION_RESULT_ONE,
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentOne.xls"))
            .getFile()).toPath()));
    FileS3Info fileForValidationInfoTwo = createFileS3Info(FILE_KEY_VALIDATION_RESULT_TWO,
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentTwo.xls"))
            .getFile()).toPath()));
    FileS3Info fileForValidationInfoThree = createFileS3Info(FILE_KEY_VALIDATION_RESULT_THREE,
        awsBucketForAttachments,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentThree.xls"))
            .getFile()).toPath()));
    return new InfoLedgerValidationRequest("kmsKeyArn", Arrays.asList(fileForValidationInfoOne,
        fileForValidationInfoTwo,
        fileForValidationInfoThree));
  }

  private FileS3Info createFileS3Info(String fileKey, String bucketName, byte[] fileByteArray) {
    FileS3Info fileForValidationInfo = new FileS3Info(fileKey, bucketName);
    fileForValidationInfo = awsS3FileService.storeFileProcessingResult(fileForValidationInfo, fileByteArray);
    return fileForValidationInfo;
  }
}
