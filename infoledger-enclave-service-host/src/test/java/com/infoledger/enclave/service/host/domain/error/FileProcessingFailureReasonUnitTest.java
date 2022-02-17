package com.infoledger.enclave.service.host.domain.error;

import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileProcessingFailureReasonUnitTest {

    private static final String FILE_NAME = "someFile.txt";
    private static final String FAILURE_REASON = "Unsupported file format.";

    @Test
    void givenFullyPopulatedFileProcessingFailureReasonWhenCallGettersThenGetExpectedValues() {
        // Given
        FileProcessingFailureReason reason = new FileProcessingFailureReason(FILE_NAME,
                FAILURE_REASON);

        // When && Then
        assertThat(reason.getFileName()).isSameAs(FILE_NAME);
        assertThat(reason.getFailureReason()).isSameAs(FAILURE_REASON);
    }
}
