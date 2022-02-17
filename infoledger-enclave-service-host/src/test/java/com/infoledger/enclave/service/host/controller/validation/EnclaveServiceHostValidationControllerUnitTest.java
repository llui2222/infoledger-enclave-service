package com.infoledger.enclave.service.host.controller.validation;

import com.google.common.collect.ImmutableList;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.request.validation.InfoLedgerValidationRequest;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.ValidationEnclaveClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EnclaveServiceHostValidationController}
 */
class EnclaveServiceHostValidationControllerUnitTest {
  private static final String KMS_KEY_ARN = "kmsKeyArn";

  @Mock
  private ValidationEnclaveClientService validationEnclaveClientService;

  @InjectMocks
  private EnclaveServiceHostValidationController controller;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void givenInfoLedgerValidationControllerWhenValidationFlowPassSuccessfullyThenGetSuccessResponse() throws IOException, InfoLedgerEntityNotFoundException {
    // Given
    FileS3Info fileForValidationInfo = mock(FileS3Info.class);
    InfoLedgerValidationRequest infoLedgerValidationRequest = mock(InfoLedgerValidationRequest.class);
    when(infoLedgerValidationRequest.getKmsKeyArn()).thenReturn(KMS_KEY_ARN);
    when(infoLedgerValidationRequest.getAttachmentFilesS3Infos()).thenReturn(Collections.singletonList(fileForValidationInfo));
    InfoLedgerValidationResponse response = mock(InfoLedgerValidationResponse.class);
    when(response.getStatus()).thenReturn(Status.OK);
    when(response.getValidationResultsPerFile()).thenReturn(Map.of("fileName", mock(ValidationResponse.class)));
    when(validationEnclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        Collections.singletonList(fileForValidationInfo))).thenReturn(response);

    // When
    ResponseEntity<InfoLedgerValidationResponse> responseEntity = controller.validate(infoLedgerValidationRequest);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    InfoLedgerValidationResponse actualResponse = responseEntity.getBody();
    assertThat(actualResponse).isSameAs(response);
    assertThat(actualResponse.getStatus()).isEqualTo(Status.OK);
    assertThat(actualResponse.getValidationFailures()).isEmpty();
    assertThat(actualResponse.getValidationResultsPerFile()).hasSize(1);
  }

  @Test
  void givenInfoLedgerValidationControllerWhenValidationFlowFailsThenGetFailedResponse() throws IOException, InfoLedgerEntityNotFoundException {
    // Given
    FileS3Info fileForValidationInfo = mock(FileS3Info.class);
    InfoLedgerValidationRequest infoLedgerValidationRequest = mock(InfoLedgerValidationRequest.class);
    when(infoLedgerValidationRequest.getKmsKeyArn()).thenReturn(KMS_KEY_ARN);
    List<FileS3Info> fileS3Infos = Collections.singletonList(fileForValidationInfo);
    when(infoLedgerValidationRequest.getAttachmentFilesS3Infos()).thenReturn(fileS3Infos);
    InfoLedgerValidationResponse response = mock(InfoLedgerValidationResponse.class);
    when(response.getStatus()).thenReturn(Status.FAILED);
    ImmutableList<FileProcessingFailureReason> failureReasons = ImmutableList.of(mock(FileProcessingFailureReason.class));
    when(response.getValidationFailures()).thenReturn(failureReasons);
    when(validationEnclaveClientService.validateAttachmentsData(KMS_KEY_ARN,
        fileS3Infos)).thenReturn(response);

    // When
    ResponseEntity<InfoLedgerValidationResponse> responseEntity = controller.validate(infoLedgerValidationRequest);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    InfoLedgerValidationResponse actualResponse = responseEntity.getBody();
    assertThat(actualResponse).isSameAs(response);
    assertThat(actualResponse.getStatus()).isEqualTo(Status.FAILED);
    assertThat(actualResponse.getValidationFailures()).containsExactlyElementsOf(failureReasons);
  }
}
