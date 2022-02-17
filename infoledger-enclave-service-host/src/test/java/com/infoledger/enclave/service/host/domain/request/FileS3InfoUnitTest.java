package com.infoledger.enclave.service.host.domain.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link FileS3Info}
 */
class FileS3InfoUnitTest {

    private static final String FILE_KEY = "FileKey1";
    private static final String ATTACHMENTS = "attachments";

    @Test
    void givenFullyPopulatedFileS3InfoWhenCallToStringThenGetExpectedJson() {
        // Given
        FileS3Info fileForAggregationInfo = new FileS3Info(FILE_KEY,
                ATTACHMENTS);

        // When && Then
        assertThat(fileForAggregationInfo).hasToString("{\"fileKey\":\"FileKey1\", " +
                "\"bucketName\":\"attachments\"}");
    }

    @Test
    void givenFullyPopulatedFileS3InfoWhenCallGettersThenGetExpectedValues() {
        // Given
        FileS3Info fileForAggregationInfo = new FileS3Info(FILE_KEY,
                ATTACHMENTS);

        // When && Then
        assertThat(fileForAggregationInfo.getFileKey()).isSameAs(FILE_KEY);
        assertThat(fileForAggregationInfo.getBucketName()).isSameAs(ATTACHMENTS);
    }
}
