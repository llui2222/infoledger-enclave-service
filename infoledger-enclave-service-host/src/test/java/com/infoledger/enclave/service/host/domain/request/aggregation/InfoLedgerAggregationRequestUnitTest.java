package com.infoledger.enclave.service.host.domain.request.aggregation;

import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link InfoLedgerAggregationRequest}
 */
class InfoLedgerAggregationRequestUnitTest {

  private static final String KMS_KEY_ARN = "kmsKeyArn";

  @Test
  void givenFullyPopulatedRequestWhenCallToStringThenGetExpectedJson() {
    // Given
    FileS3Info fileForAggregationInfoFirst = createFileS3Info("FileKey1");
    FileS3Info fileForAggregationInfoSecond = createFileS3Info("FileKey2");
    InfoLedgerAggregationRequest request = new InfoLedgerAggregationRequest(KMS_KEY_ARN,
        Collections.singletonList(fileForAggregationInfoSecond),
        fileForAggregationInfoFirst);

    // When && Then
    assertThat(request).hasToString("{\"kmsKeyArn\":\"kmsKeyArn\", " +
        "\"attachmentFilesS3Infos\": [{\"fileKey\":\"FileKey2\", \"bucketName\":\"attachments\"}], " +
        "\"fileResultS3Info\": {\"fileKey\":\"FileKey1\", \"bucketName\":\"attachments\"}}");
  }

  @Test
  void givenFullyPopulatedRequestWhenCallGettersThenReturnExpectedValues() {
    // Given
    FileS3Info fileForAggregationInfoFirst = mock(FileS3Info.class);
    FileS3Info fileForAggregationInfoSecond = mock(FileS3Info.class);
    List<FileS3Info> attachmentFilesS3Infos = Collections.singletonList(fileForAggregationInfoSecond);
    InfoLedgerAggregationRequest request = new InfoLedgerAggregationRequest(KMS_KEY_ARN,
        attachmentFilesS3Infos,
        fileForAggregationInfoFirst);

    // When && Then
    assertThat(request.getKmsKeyArn()).isSameAs(KMS_KEY_ARN);
    assertThat(request.getAttachmentFilesS3Infos()).isSameAs(attachmentFilesS3Infos);
    assertThat(request.getFileResultS3Info()).isSameAs(fileForAggregationInfoFirst);
  }

  private FileS3Info createFileS3Info(String fileKey) {
    return new FileS3Info(fileKey, "attachments");
  }
}
