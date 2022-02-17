package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.infoledger.aggregation.enclave.client.EnclaveAggregationException;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
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

class AggregationEnclaveClientServiceImplUnitTest {

  private static final byte[] FILE_BYTES = {12, 12, 32, 36};
  private static final String FILE_KEY_ONE = "fileKeyOne";
  private static final String FILE_KEY_TWO = "fileKeyTwo";
  private static final String FILE_KEY_THREE = "fileKeyThree";
  private static final String BUCKET_NAME = "bucketName";
  private static final String ATTACHMENTS_FILE_NAME_ONE = "messageAttachmentOne.xls";
  private static final String ATTACHMENTS_FILE_NAME_TWO = "messageAttachmentTwo.xls";
  private static final String ATTACHMENTS_FILE_NAME_THREE = "messageAttachmentThree.xls";
  private static final String AGGREGATION_RESULT_FILE_NAME = "aggregationResult.xls";
  private static final String KMS_KEY_ARN = "kms_key_arn";

  @Mock
  private AwsS3FileService awsS3FileService;
  @Mock
  private EnclaveClient enclaveClient;

  @InjectMocks
  private AggregationEnclaveClientServiceImpl enclaveClientService;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    testAuth();
  }

  @Test
  void givenAllNeededInfoForAggregationWithNullAggregationResultInS3WhenCallForAggregateThenReturnUpdatedAggregationResultFileInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
    when(fileForAggregationInfo.getBucketName()).thenReturn(BUCKET_NAME);
    when(fileForAggregationInfo.getFileKey()).thenReturn(FILE_KEY_ONE);
    S3Object s3ObjectAttachment = prepareAndGetS3Object(FILE_KEY_ONE, ATTACHMENTS_FILE_NAME_ONE);
    when(awsS3FileService.downloadFile(fileForAggregationInfo, true)).thenReturn(s3ObjectAttachment);
    when(awsS3FileService.storeFileProcessingResult(any(FileS3Info.class), any(byte[].class))).thenReturn(fileForAggregationInfo);
    when(enclaveClient.aggregate(any(BasicSessionCredentials.class), anyString(), any(byte[].class), nullable(byte[].class)))
        .thenReturn(FILE_BYTES);

    // When && Then
    InfoLedgerAggregationResponse response = enclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfo),
        fileForAggregationInfo);
    assertThat(response.getStatus()).isEqualTo(Status.OK);
  }

  @Test
  void givenAllNeededInfoForAggregationWhenCallForAggregateThenReturnUpdatedAggregationResultFileInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfoOne = mock(FileS3Info.class);
    FileS3Info fileForAggregationInfoTwo = mock(FileS3Info.class);
    FileS3Info fileForAggregationInfoThree = mock(FileS3Info.class);
    prepareWorkAroundForTest(fileForAggregationInfoOne,
        fileForAggregationInfoTwo,
        fileForAggregationInfoThree);
    when(enclaveClient.aggregate(any(AWSCredentials.class), anyString(), any(byte[].class), any(byte[].class)))
        .thenReturn(FILE_BYTES);

    // When && Then
    InfoLedgerAggregationResponse response = enclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForAggregationInfoOne,
            fileForAggregationInfoTwo,
            fileForAggregationInfoThree),
        fileForAggregationInfoOne);
    assertThat(response.getStatus()).isEqualTo(Status.OK);
  }

  @Test
  void givenAllNeededInfoForAggregationWhenCallForAggregateOneFileIsNotAggregatedDueToFailureThenReturnResponseWithAllInfo() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfoOne = mock(FileS3Info.class);
    FileS3Info fileForAggregationInfoTwo = mock(FileS3Info.class);
    FileS3Info fileForAggregationInfoThree = mock(FileS3Info.class);
    prepareWorkAroundForTest(fileForAggregationInfoOne,
        fileForAggregationInfoTwo,
        fileForAggregationInfoThree);
    when(enclaveClient.aggregate(any(AWSCredentials.class), anyString(), any(byte[].class), any(byte[].class)))
        .thenReturn(FILE_BYTES)
        .thenReturn(FILE_BYTES)
        .thenThrow(new EnclaveAggregationException("File has unsupported format."));

    // When && Then
    InfoLedgerAggregationResponse response = enclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Arrays.asList(fileForAggregationInfoOne,
            fileForAggregationInfoTwo,
            fileForAggregationInfoThree),
        fileForAggregationInfoOne);
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
    assertThat(response.getAggregationFailures()).hasSize(1);
  }

  @Test
  void givenAllNeededInfoForAggregationWhenCallForAggregateDoesNotPassThenReturnNull() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
    S3Object s3ObjectAttachment = prepareAndGetS3Object(FILE_KEY_ONE, ATTACHMENTS_FILE_NAME_ONE);
    when(awsS3FileService.downloadFile(fileForAggregationInfo, true)).thenReturn(s3ObjectAttachment);
    when(awsS3FileService.storeFileProcessingResult(any(FileS3Info.class), any(byte[].class))).thenReturn(fileForAggregationInfo);
    when(enclaveClient.aggregate(any(AWSCredentials.class), anyString(), any(byte[].class), nullable(byte[].class)))
        .thenThrow(new EnclaveAggregationException("can not aggregate"));

    // When && Then
    InfoLedgerAggregationResponse response = enclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfo),
        fileForAggregationInfo);
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
  }

  private void prepareWorkAroundForTest(FileS3Info fileForAggregationInfoOne,
                                        FileS3Info fileForAggregationInfoTwo,
                                        FileS3Info fileForAggregationInfoThree) throws InfoLedgerEntityNotFoundException {
    S3Object s3ObjectAttachmentOne = prepareAndGetS3Object(FILE_KEY_ONE, ATTACHMENTS_FILE_NAME_ONE);
    S3Object s3ObjectAttachmentTwo = prepareAndGetS3Object(FILE_KEY_TWO, ATTACHMENTS_FILE_NAME_TWO);
    S3Object s3ObjectAttachmentThree = prepareAndGetS3Object(FILE_KEY_THREE, ATTACHMENTS_FILE_NAME_THREE);
    S3Object s3ObjectAggregationResult = mock(S3Object.class);
    S3ObjectInputStream s3ObjectInputStreamAggregationResult = new S3ObjectInputStream(Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream(AGGREGATION_RESULT_FILE_NAME),
        null);
    when(s3ObjectAggregationResult.getObjectContent()).thenReturn(s3ObjectInputStreamAggregationResult);
    when(awsS3FileService.downloadFile(fileForAggregationInfoOne, false)).thenReturn(s3ObjectAggregationResult);
    when(awsS3FileService.downloadFile(fileForAggregationInfoOne, true)).thenReturn(s3ObjectAttachmentOne);
    when(awsS3FileService.downloadFile(fileForAggregationInfoTwo, true)).thenReturn(s3ObjectAttachmentTwo);
    when(awsS3FileService.downloadFile(fileForAggregationInfoThree, true)).thenReturn(s3ObjectAttachmentThree);
    when(awsS3FileService.storeFileProcessingResult(any(FileS3Info.class), any(byte[].class))).thenReturn(fileForAggregationInfoOne);
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
