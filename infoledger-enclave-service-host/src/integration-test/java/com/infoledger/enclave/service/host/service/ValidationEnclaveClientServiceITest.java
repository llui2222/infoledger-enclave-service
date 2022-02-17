package com.infoledger.enclave.service.host.service;

import com.infoledger.enclave.service.host.InfoLedgerEnclaveServiceHostApplication;
import com.infoledger.enclave.service.host.TestApplicationContextInitializer;
import com.infoledger.enclave.service.host.config.TestConfig;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import static com.infoledger.enclave.service.host.config.StubEnclaveClient.setValidationFails;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = InfoLedgerEnclaveServiceHostApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class ValidationEnclaveClientServiceITest {

  private static final String FILE_KEY_ONE = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.one.not.validated";
  private static final String FILE_KEY_TWO = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.two.not.validated";
  private static final String FILE_KEY_THREE = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment.three.not.validated";
  private static final String KMS_KEY_ARN = "kms_key_arn";

  @Value("${amazon.aws.bucket.attachments:}")
  private String awsBucketForAttachments;

  @Autowired
  private AwsS3FileService awsS3FileService;

  @Autowired
  private ValidationEnclaveClientService validationEnclaveClientService;

  @Test
  void testValidateSuccessfully() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    TestConfig.mockSecurityContextHolderAuthentication();
    FileS3Info fileForValidationInfoAttachmentOne = prepareAndGetFileS3Info(FILE_KEY_ONE, "testfiles/messageAttachmentOne.xls");
    FileS3Info fileForValidationInfoAttachmentTwo = prepareAndGetFileS3Info(FILE_KEY_TWO, "testfiles/messageAttachmentTwo.xls");
    FileS3Info fileForValidationInfoAttachmentThree = prepareAndGetFileS3Info(FILE_KEY_THREE, "testfiles/messageAttachmentThree.xls");

    setValidationFails(false);

    // When && Then
    InfoLedgerValidationResponse response = validationEnclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForValidationInfoAttachmentOne,
            fileForValidationInfoAttachmentTwo,
            fileForValidationInfoAttachmentThree));
    assertThat(response.getStatus()).isEqualTo(Status.OK);
    assertThat(response.getValidationFailures()).isNull();
    assertThat(response.getValidationResultsPerFile()).hasSize(3);
  }

  @Test
  void testValidateWithValidationFailures() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    TestConfig.mockSecurityContextHolderAuthentication();
    FileS3Info fileForValidationInfoAttachmentOne = prepareAndGetFileS3Info(FILE_KEY_ONE, "testfiles/messageAttachmentOne.xls");
    FileS3Info fileForValidationInfoAttachmentTwo = prepareAndGetFileS3Info(FILE_KEY_TWO, "testfiles/messageAttachmentTwo.xls");
    FileS3Info fileForValidationInfoAttachmentThree = prepareAndGetFileS3Info(FILE_KEY_THREE, "testfiles/messageAttachmentThree.xls");

    setValidationFails(true);
    // When && Then
    InfoLedgerValidationResponse response = validationEnclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForValidationInfoAttachmentOne,
            fileForValidationInfoAttachmentTwo,
            fileForValidationInfoAttachmentThree));
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
    assertThat(response.getValidationFailures()).isEmpty();
    assertThat(response.getValidationResultsPerFile()).hasSize(3);
  }

  private FileS3Info prepareAndGetFileS3Info(String fileKey, String filePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    FileS3Info fileForValidationInfoAttachment = new FileS3Info(fileKey, awsBucketForAttachments);
    fileForValidationInfoAttachment = awsS3FileService.storeFileProcessingResult(fileForValidationInfoAttachment,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource(filePath))
            .getFile()).toPath()));
    return fileForValidationInfoAttachment;
  }
}
