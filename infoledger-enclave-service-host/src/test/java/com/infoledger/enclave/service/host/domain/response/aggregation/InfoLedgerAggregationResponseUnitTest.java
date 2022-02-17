package com.infoledger.enclave.service.host.domain.response.aggregation;

import com.google.common.collect.ImmutableList;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InfoLedgerAggregationResponseUnitTest {

    @Test
    void givenInfoLedgerAggregationResponseWhenCallMethodOkThenGetExpectedValues() {
        // Given
        FileS3Info aggregatedFileInfo = mock(FileS3Info.class);

        // When
        InfoLedgerAggregationResponse response = InfoLedgerAggregationResponse.ok(aggregatedFileInfo);

        // Then
        assertThat(response.getStatus()).isEqualTo(Status.OK);
        assertThat(response.getAggregatedFileInfo()).isSameAs(aggregatedFileInfo);
        assertThat(response.getAggregationFailures()).isNull();
    }

    @Test
    void givenInfoLedgerAggregationResponseWhenCallMethodPartialOkThenGetExpectedValues() {
        // Given
        FileS3Info aggregatedFileInfo = mock(FileS3Info.class);
        ImmutableList<FileProcessingFailureReason> failureReasons = ImmutableList.of(mock(FileProcessingFailureReason.class));

        // When
        InfoLedgerAggregationResponse response = InfoLedgerAggregationResponse.partiallyOk(aggregatedFileInfo, failureReasons);

        // Then
        assertThat(response.getStatus()).isEqualTo(Status.OK);
        assertThat(response.getAggregatedFileInfo()).isSameAs(aggregatedFileInfo);
        assertThat(response.getAggregationFailures()).containsExactlyElementsOf(failureReasons);
    }

    @Test
    void givenInfoLedgerAggregationResponseWhenCallMethodFailedThenGetExpectedValues() {
        // Given
        ImmutableList<FileProcessingFailureReason> failureReasons = ImmutableList.of(mock(FileProcessingFailureReason.class));

        // When
        InfoLedgerAggregationResponse response = InfoLedgerAggregationResponse.failed(failureReasons);

        // Then
        assertThat(response.getStatus()).isEqualTo(Status.FAILED);
        assertThat(response.getAggregatedFileInfo()).isNull();
        assertThat(response.getAggregationFailures()).containsExactlyElementsOf(failureReasons);
    }
}
