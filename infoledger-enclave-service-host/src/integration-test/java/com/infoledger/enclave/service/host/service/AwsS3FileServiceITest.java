package com.infoledger.enclave.service.host.service;

import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.enclave.service.host.InfoLedgerEnclaveServiceHostApplication;
import com.infoledger.enclave.service.host.TestApplicationContextInitializer;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AwsS3FileService}
 */
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"cloud"})
@SpringBootTest(classes = InfoLedgerEnclaveServiceHostApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class AwsS3FileServiceITest {

    private static final byte[] FILE_BYTES = new byte[]{122, 12, 12};
    private static final String FILE_KEY = "longtext/26e9d218-8aa1-11eb-8dcd-0242ac130003#1618606381047.attachment";

    @Value("${amazon.aws.bucket.attachments:}")
    private String awsBucketForAttachments;

    @Autowired
    private AwsS3FileService awsS3FileService;

    @Test
    void givenAttachmentFileWhenCallUploadAttachmentFileThenItSuccessfullySavedAndPathToItReturned() throws IOException {
        // Given
        FileS3Info fileForAggregationInfoAggergationResult = new FileS3Info(FILE_KEY,
                awsBucketForAttachments);
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("testfiles/Encryptedlongtext.txt"))
                .getFile());
        byte[] bytes = Files.readAllBytes(file.toPath());

        // When
        FileS3Info fileForAggregationInfo = awsS3FileService.storeFileProcessingResult(fileForAggregationInfoAggergationResult, bytes);

        // Then
        assertThat(fileForAggregationInfo.getFileKey()).endsWith(FILE_KEY);
    }

    @Test
    void givenAttachmentFilePathWhenCallDownloadAttachmentFileThenItSuccessfullyDownloadAndReturnIt() throws InfoLedgerEntityNotFoundException {
        // Given
        FileS3Info fileForAggregationInfoAttachment = new FileS3Info(FILE_KEY,
                awsBucketForAttachments);
        fileForAggregationInfoAttachment = awsS3FileService.storeFileProcessingResult(fileForAggregationInfoAttachment, FILE_BYTES);

        // When
        S3Object attachmentFileS3Object = awsS3FileService.downloadFile(fileForAggregationInfoAttachment, true);

        // Then
        assertThat(attachmentFileS3Object.getKey()).isEqualTo(fileForAggregationInfoAttachment.getFileKey());
        assertThat(attachmentFileS3Object.getBucketName()).isEqualTo(awsBucketForAttachments);
        assertThat(attachmentFileS3Object.getObjectContent()).isNotNull();
    }
}
