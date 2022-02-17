package com.infoledger.enclave.service.host.domain.request.validation;

import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link InfoLedgerValidationRequest}
 */
class InfoLedgerValidationRequestUnitTest {

    private static final String KMS_KEY_ARN = "kmsKeyArn";

    @Test
    void givenFullyPopulatedRequestWhenCallToStringThenGetExpectedJson() {
        // Given
        FileS3Info fileForValidationInfoSecond = createFileS3Info("FileKey2");
        InfoLedgerValidationRequest request = new InfoLedgerValidationRequest(KMS_KEY_ARN,
            Collections.singletonList(fileForValidationInfoSecond));

        // When && Then
        assertThat(request).hasToString("{\"kmsKeyArn\":\"kmsKeyArn\", " +
            "\"attachmentFilesS3Infos\": [{\"fileKey\":\"FileKey2\", \"bucketName\":\"attachments\"}]}");
    }

    @Test
    void givenFullyPopulatedRequestWhenCallGettersThenReturnExpectedValues() {
        // Given
        FileS3Info fileForValidationInfoSecond = mock(FileS3Info.class);
        List<FileS3Info> attachmentFilesS3Infos = Collections.singletonList(fileForValidationInfoSecond);
        InfoLedgerValidationRequest request = new InfoLedgerValidationRequest(KMS_KEY_ARN,
            attachmentFilesS3Infos);

        // When && Then
        assertThat(request.getKmsKeyArn()).isSameAs(KMS_KEY_ARN);
        assertThat(request.getAttachmentFilesS3Infos()).isSameAs(attachmentFilesS3Infos);
    }

    private FileS3Info createFileS3Info(String fileKey) {
        return new FileS3Info(fileKey, "attachments");
    }
}
