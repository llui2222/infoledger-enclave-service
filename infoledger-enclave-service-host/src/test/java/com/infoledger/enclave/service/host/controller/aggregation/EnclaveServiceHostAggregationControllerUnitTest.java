package com.infoledger.enclave.service.host.controller.aggregation;

import com.google.common.collect.ImmutableList;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.request.aggregation.InfoLedgerAggregationRequest;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AggregationEnclaveClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EnclaveServiceHostAggregationController}
 */
class EnclaveServiceHostAggregationControllerUnitTest {

  private static final String KMS_KEY_ARN = "kmsKeyArn";

  @Mock
  private AggregationEnclaveClientService aggregationEnclaveClientService;

  @InjectMocks
  private EnclaveServiceHostAggregationController controller;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void givenInfoLedgerAggregationControllerWhenAggregationFlowPassSuccessfullyThenGetSuccessResponse() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
    InfoLedgerAggregationRequest infoLedgerAggregationRequest = mock(InfoLedgerAggregationRequest.class);
    when(infoLedgerAggregationRequest.getKmsKeyArn()).thenReturn(KMS_KEY_ARN);
    when(infoLedgerAggregationRequest.getAttachmentFilesS3Infos()).thenReturn(Collections.singletonList(fileForAggregationInfo));
    when(infoLedgerAggregationRequest.getFileResultS3Info()).thenReturn(fileForAggregationInfo);
    InfoLedgerAggregationResponse response = mock(InfoLedgerAggregationResponse.class);
    when(response.getAggregatedFileInfo()).thenReturn(fileForAggregationInfo);
    when(response.getStatus()).thenReturn(Status.OK);
    when(aggregationEnclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfo),
        fileForAggregationInfo)).thenReturn(response);

    // When
    ResponseEntity<InfoLedgerAggregationResponse> responseEntity = controller.aggregate(infoLedgerAggregationRequest);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    InfoLedgerAggregationResponse actualResponse = responseEntity.getBody();
    assertThat(actualResponse).isSameAs(response);
    assertThat(actualResponse.getAggregatedFileInfo()).isSameAs(fileForAggregationInfo);
    assertThat(actualResponse.getStatus()).isEqualTo(Status.OK);
    assertThat(actualResponse.getAggregationFailures()).isEmpty();
  }

  @Test
  void givenInfoLedgerAggregationControllerWhenAggregationFlowFailsThenGetFailedResponse() throws InfoLedgerEntityNotFoundException,
      IOException {
    // Given
    FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
    InfoLedgerAggregationRequest infoLedgerAggregationRequest = mock(InfoLedgerAggregationRequest.class);
    when(infoLedgerAggregationRequest.getKmsKeyArn()).thenReturn(KMS_KEY_ARN);
    when(infoLedgerAggregationRequest.getAttachmentFilesS3Infos()).thenReturn(Collections.singletonList(fileForAggregationInfo));
    when(infoLedgerAggregationRequest.getFileResultS3Info()).thenReturn(fileForAggregationInfo);
    InfoLedgerAggregationResponse response = mock(InfoLedgerAggregationResponse.class);
    when(response.getStatus()).thenReturn(Status.FAILED);
    ImmutableList<FileProcessingFailureReason> failureReasons = ImmutableList.of(mock(FileProcessingFailureReason.class));
    when(response.getAggregationFailures()).thenReturn(failureReasons);
    when(aggregationEnclaveClientService.aggregateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfo),
        fileForAggregationInfo)).thenReturn(response);

    // When
    ResponseEntity<InfoLedgerAggregationResponse> responseEntity = controller.aggregate(infoLedgerAggregationRequest);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    InfoLedgerAggregationResponse actualResponse = responseEntity.getBody();
    assertThat(actualResponse).isSameAs(response);
    assertThat(actualResponse.getAggregatedFileInfo()).isNull();
    assertThat(actualResponse.getStatus()).isEqualTo(Status.FAILED);
    assertThat(actualResponse.getAggregationFailures()).containsExactlyElementsOf(failureReasons);
  }
}
