package com.infoledger.enclave.service.host.domain.response.validation;

import com.google.common.collect.ImmutableList;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InfoLedgerValidationResponseUnitTest {

  @Test
  void givenInfoLedgerValidationResponseWhenCallMethodOkThenGetExpectedValues() {
    // When
    InfoLedgerValidationResponse response = InfoLedgerValidationResponse.ok(null);

    // Then
    assertThat(response.getStatus()).isEqualTo(Status.OK);
    assertThat(response.getValidationFailures()).isNull();
    assertThat(response.getValidationResultsPerFile()).isNull();
  }

  @Test
  void givenInfoLedgerValidationResponseWhenCallMethodFailedThenGetExpectedValues() {
    // Given
    String fileName = "someBrokenFile";
    ValidationResponse validationResponse = mock(ValidationResponse.class);
    ImmutableList<FileProcessingFailureReason> failureReasons = ImmutableList.of(mock(FileProcessingFailureReason.class));

    // When
    InfoLedgerValidationResponse response = InfoLedgerValidationResponse.failed(Map.of("someBrokenFile", validationResponse),
        failureReasons);

    // Then
    assertThat(response.getStatus()).isEqualTo(Status.FAILED);
    assertThat(response.getValidationFailures()).containsExactlyElementsOf(failureReasons);
    assertThat(response.getValidationResultsPerFile()).containsExactlyEntriesOf(Map.of(fileName, validationResponse));
  }
}
