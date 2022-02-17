package com.infoledger.enclave.service.host.service;

import com.infoledger.enclave.service.host.InfoLedgerEnclaveServiceHostApplication;
import com.infoledger.enclave.service.host.TestApplicationContextInitializer;
import com.infoledger.enclave.service.host.config.TestConfig;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
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
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = InfoLedgerEnclaveServiceHostApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class AggregationEnclaveClientServiceITest {

  private static final String FILE_KEY = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment";
  private static final String FILE_KEY_AGGREGATED = "1617904825262998/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.aggregations";
  private static final String KMS_KEY_ARN = "kms_key_arn";

  @Value("${amazon.aws.bucket.attachments:}")
  private String awsBucketForAttachments;

  @Value("${amazon.aws.bucket.aggregations:}")
  private String awsBucketForAggregations;

  @Autowired
  private AwsS3FileService awsS3FileService;

  @Autowired
  private AggregationEnclaveClientService aggregationEnclaveClientService;

  @Test
  void testSuccessfulAggregation() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    TestConfig.mockSecurityContextHolderAuthentication();
    ClassLoader classLoader = getClass().getClassLoader();
    FileS3Info fileForAggregationInfoAttachment = new FileS3Info(FILE_KEY, awsBucketForAttachments);
    fileForAggregationInfoAttachment = awsS3FileService.storeFileProcessingResult(fileForAggregationInfoAttachment,
        Files.readAllBytes(new File(Objects.requireNonNull(classLoader.getResource("testfiles/messageAttachmentOne.xls"))
            .getFile()).toPath()));
    FileS3Info fileForAggregationInfoResult = new FileS3Info(FILE_KEY_AGGREGATED, awsBucketForAggregations);

    // When && Then
    InfoLedgerAggregationResponse response = aggregationEnclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfoAttachment),
        fileForAggregationInfoResult);
    assertThat(response.getStatus()).isEqualTo(Status.OK);
    assertThat(response.getAggregatedFileInfo()).isSameAs(fileForAggregationInfoResult);
    assertThat(response.getAggregationFailures()).isNull();
  }
}
