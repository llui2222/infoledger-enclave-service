package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.infoledger.aggregation.enclave.client.EnclaveAggregationException;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.aggregation.enclave.client.EnclaveValidationException;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
import com.infoledger.enclave.service.host.stubclient.StubEnclaveValidationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.infoledger.enclave.service.host.service.impl.TestUtil.testAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationEnclaveClientServiceImplUnitTest {

  private static final String BUCKET_NAME = "bucketName";
  private static final String FILE_HAS_UNSUPPORTED_FORMAT = "File has unsupported format.";
  private static final String KMS_KEY_ARN = "kms_key_arn";
  private static final String FILE_KEY_ONE = "fileKeyOne";
  private static final String FILE_KEY_TWO = "fileKeyTwo";
  private static final String FILE_KEY_THREE = "fileKeyThree";
  private static final String ATTACHMENTS_FILE_NAME_ONE = "messageAttachmentOne.xls";
  private static final String ATTACHMENTS_FILE_NAME_TWO = "messageAttachmentTwo.xls";
  private static final String ATTACHMENTS_FILE_NAME_THREE = "messageAttachmentThree.xls";

  @Mock
  private AwsS3FileService awsS3FileService;
  @Mock
  private EnclaveClient enclaveClient;

  @InjectMocks
  private ValidationEnclaveClientServiceImpl enclaveClientService;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    testAuth();
  }

  @Test
  void givenAllNeededInfoForValidationWithNullValidationResultInS3WhenCallForAggregateThenReturnUpdatedValidationResultFileInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForValidationInfo = mock(FileS3Info.class);
    when(fileForValidationInfo.getBucketName()).thenReturn(BUCKET_NAME);
    when(fileForValidationInfo.getFileKey()).thenReturn(FILE_KEY_ONE);
    S3Object s3ObjectAttachment = mock(S3Object.class);
    when(s3ObjectAttachment.getKey()).thenReturn(FILE_KEY_ONE);
    S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream(ATTACHMENTS_FILE_NAME_ONE),
        null);
    when(s3ObjectAttachment.getObjectContent()).thenReturn(s3ObjectInputStream);
    when(awsS3FileService.downloadFile(fileForValidationInfo, true)).thenReturn(s3ObjectAttachment);
    when(awsS3FileService.storeFileProcessingResult(any(FileS3Info.class), any(byte[].class))).thenReturn(fileForValidationInfo);
    when(enclaveClient.validate(any(AWSCredentials.class), anyString(), any(byte[].class)))
        .thenReturn(ValidationResponse.ok());

    // When && Then
    InfoLedgerValidationResponse response = enclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForValidationInfo));
    assertThat(response.getStatus()).isEqualTo(Status.OK);
  }

  @Test
  void givenAllNeededInfoForValidationWhenCallForAggregateThenReturnUpdatedValidationResultFileInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForValidationInfoOne = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoTwo = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoThree = mock(FileS3Info.class);
    prepareWorkAroundForTest(fileForValidationInfoOne,
        fileForValidationInfoTwo,
        fileForValidationInfoThree);
    when(enclaveClient.validate(any(BasicSessionCredentials.class), anyString(), nullable(byte[].class)))
        .thenReturn(ValidationResponse.ok());

    // When && Then
    InfoLedgerValidationResponse response = enclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForValidationInfoOne,
            fileForValidationInfoTwo,
            fileForValidationInfoThree));
    assertThat(response.getStatus()).isEqualTo(Status.OK);
  }

  @Test
  void givenAllNeededInfoForValidationWhenCallForAggregateOneFileIsNotAggregatedDueToFailureThenReturnResponseWithAllInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForValidationInfoOne = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoTwo = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoThree = mock(FileS3Info.class);
    prepareWorkAroundForTest(fileForValidationInfoOne,
        fileForValidationInfoTwo,
        fileForValidationInfoThree);
    when(enclaveClient.validate(any(AWSCredentials.class), anyString(), any(byte[].class)))
        .thenReturn(ValidationResponse.ok())
        .thenReturn(ValidationResponse.ok())
        .thenThrow(new EnclaveValidationException(FILE_HAS_UNSUPPORTED_FORMAT));

    // When && Then
    InfoLedgerValidationResponse response = enclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForValidationInfoOne,
            fileForValidationInfoTwo,
            fileForValidationInfoThree));
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
    assertThat(response.getValidationFailures()).hasSize(1);
    FileProcessingFailureReason expectedFailureReason = new FileProcessingFailureReason(FILE_KEY_THREE,
        FILE_HAS_UNSUPPORTED_FORMAT);
    assertThat(response.getValidationFailures()).containsExactly(expectedFailureReason);
  }

  @Test
  void givenAllNeededInfoForValidationWhenCallForAggregateDoesNotPassThenReturnNull() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForValidationInfoOne = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoTwo = mock(FileS3Info.class);
    FileS3Info fileForValidationInfoThree = mock(FileS3Info.class);
    prepareWorkAroundForTest(fileForValidationInfoOne,
        fileForValidationInfoTwo,
        fileForValidationInfoThree);
    when(enclaveClient.validate(any(AWSCredentials.class), anyString(), any(byte[].class)))
        .thenThrow(new EnclaveValidationException(FILE_HAS_UNSUPPORTED_FORMAT));

    // When && Then
    InfoLedgerValidationResponse response = enclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForValidationInfoOne,
            fileForValidationInfoTwo,
            fileForValidationInfoThree));
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
    FileProcessingFailureReason expectedFailureReasonOne = new FileProcessingFailureReason(FILE_KEY_ONE,
        FILE_HAS_UNSUPPORTED_FORMAT);
    FileProcessingFailureReason expectedFailureReasonTwo = new FileProcessingFailureReason(FILE_KEY_TWO,
        FILE_HAS_UNSUPPORTED_FORMAT);
    FileProcessingFailureReason expectedFailureReasonThree = new FileProcessingFailureReason(FILE_KEY_THREE,
        FILE_HAS_UNSUPPORTED_FORMAT);
    assertThat(response.getValidationFailures()).containsExactly(expectedFailureReasonOne,
        expectedFailureReasonTwo,
        expectedFailureReasonThree);
  }

  private void prepareWorkAroundForTest(FileS3Info fileForAggregationInfoOne,
                                        FileS3Info fileForAggregationInfoTwo,
                                        FileS3Info fileForAggregationInfoThree) throws InfoLedgerEntityNotFoundException {
    S3Object s3ObjectAttachmentOne = prepareAndGetS3Object(FILE_KEY_ONE, ATTACHMENTS_FILE_NAME_ONE);
    S3Object s3ObjectAttachmentTwo = prepareAndGetS3Object(FILE_KEY_TWO, ATTACHMENTS_FILE_NAME_TWO);
    S3Object s3ObjectAttachmentThree = prepareAndGetS3Object(FILE_KEY_THREE, ATTACHMENTS_FILE_NAME_THREE);

    when(awsS3FileService.downloadFile(fileForAggregationInfoOne, true)).thenReturn(s3ObjectAttachmentOne);
    when(awsS3FileService.downloadFile(fileForAggregationInfoTwo, true)).thenReturn(s3ObjectAttachmentTwo);
    when(awsS3FileService.downloadFile(fileForAggregationInfoThree, true)).thenReturn(s3ObjectAttachmentThree);
  }

  private S3Object prepareAndGetS3Object(String fileKey, String fileName) {
    S3Object s3ObjectAttachment = mock(S3Object.class);
    when(s3ObjectAttachment.getKey()).thenReturn(fileKey);
    S3ObjectInputStream s3ObjectInputStreamAttachmentOne = new S3ObjectInputStream(Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream(fileName),
        null);
    when(s3ObjectAttachment.getObjectContent()).thenReturn(s3ObjectInputStreamAttachmentOne);
    when(s3ObjectAttachment.getKey()).thenReturn(fileKey);
    return s3ObjectAttachment;
  }
}
