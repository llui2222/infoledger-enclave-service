package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link AwsS3FileServiceImpl}
 */
class AwsS3FileServiceImplUnitTest {

    private static final byte[] ATTACHMENT_FILE_BYTES = new byte[]{12, 12, 12};
    private static final String AGGREGATIONS_BUCKET_NAME = "aggregations";
    private static final String FILE_KEY = "data";
    private static final String BUCKET_NAME = "bucketName";
    private static final String FILE_URL = "https://www.someurl.com";
    private static final String CAN_NOT_LOAD_ENTITY_EXCEPTION_MESSAGE = "can not load entity";

    @Mock
    private AmazonS3 amazonS3;
    @InjectMocks
    private AwsS3FileServiceImpl awsS3FileService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenAttachmentMultipartFileWhenCallUploadAttachmentFileThenFileSuccessfullyStoredOnS3() {
        // Given
        FileS3Info forAggregationInfo = new FileS3Info(FILE_KEY, AGGREGATIONS_BUCKET_NAME);
        PutObjectResult putObjectResult = mock(PutObjectResult.class);
        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);

        // When
        FileS3Info fileForAggregationInfo = awsS3FileService.storeFileProcessingResult(forAggregationInfo,
                ATTACHMENT_FILE_BYTES);

        // Then
        assertThat(fileForAggregationInfo.getFileKey()).isEqualTo(FILE_KEY);
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test
    void givenAttachmentMultipartFileIsEmptyWhenCallUploadAttachmentFileThenFileNotStoredOnS3AnndMethodReturnnNull() {
        // Given
        FileS3Info forAggregationInfo = new FileS3Info(FILE_KEY, AGGREGATIONS_BUCKET_NAME);
        PutObjectResult putObjectResult = mock(PutObjectResult.class);
        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);

        // When
        FileS3Info fileForAggregationInfo = awsS3FileService.storeFileProcessingResult(forAggregationInfo,
                new byte[]{});

        // Then
        assertThat(fileForAggregationInfo).isNull();
        verifyNoInteractions(amazonS3);
    }

    @Test
    void givenAttachmentMultipartFileWhenCallUploadAttachmentFileThenExceptionIsThrown() {
        // Given
        FileS3Info forAggregationInfo = new FileS3Info(FILE_KEY, AGGREGATIONS_BUCKET_NAME);
        doThrow(new AmazonServiceException(CAN_NOT_LOAD_ENTITY_EXCEPTION_MESSAGE)).when(amazonS3).putObject(any(PutObjectRequest.class));

        // When
        FileS3Info fileForAggregationInfo = awsS3FileService.storeFileProcessingResult(forAggregationInfo,
                ATTACHMENT_FILE_BYTES);

        // Then
        assertThat(fileForAggregationInfo).isNull();
    }

    @Test
    void givenAttachmentFileS3PathWhenCallDownloadAttachmentFileThenFileSuccessfullyDownloadedFromS3() throws IOException,
            InfoLedgerEntityNotFoundException {
        // Given
        FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
        when(fileForAggregationInfo.getBucketName()).thenReturn(BUCKET_NAME);
        when(fileForAggregationInfo.getFileKey()).thenReturn(FILE_KEY);
        S3Object s3Object = mock(S3Object.class);
        URL url = new URL(FILE_URL);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(url);
        when(amazonS3.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);

        // When
        S3Object attachmentFileS3Object = awsS3FileService.downloadFile(fileForAggregationInfo, true);

        // Then
        assertThat(attachmentFileS3Object).isSameAs(s3Object);
        verify(amazonS3).getObject(any(GetObjectRequest.class));
    }

    @Test
    void givenAttachmentResultFileS3PathWhenCallDownloadAttachmentFileThenFileNotDownloadedFromS3() throws IOException,
            InfoLedgerEntityNotFoundException {
        // Given
        FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
        when(fileForAggregationInfo.getBucketName()).thenReturn(BUCKET_NAME);
        when(fileForAggregationInfo.getFileKey()).thenReturn(FILE_KEY);
        URL url = new URL(FILE_URL);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(url);

        // When
        S3Object attachmentFileS3Object = awsS3FileService.downloadFile(fileForAggregationInfo, false);

        // Then
        assertThat(attachmentFileS3Object).isNull();
        verify(amazonS3).getObject(any(GetObjectRequest.class));
    }

    @Test
    void givenAttachmentResultFileS3PathWhenCallDownloadAttachmentResultFileThenFileNotDownloadedAndExceptionThrown() throws MalformedURLException {
        // Given
        FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
        when(fileForAggregationInfo.getBucketName()).thenReturn(BUCKET_NAME);
        when(fileForAggregationInfo.getFileKey()).thenReturn(FILE_KEY);
        URL url = new URL(FILE_URL);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(url);

        // When
        InfoLedgerEntityNotFoundException exception = assertThrows(InfoLedgerEntityNotFoundException.class,
                () -> awsS3FileService.downloadFile(fileForAggregationInfo, true));

        // Then
        assertThat(exception.getMessage()).isEqualTo("file not found");
    }

    @Test
    void givenAttachmentResultFileS3PathWhenCallDownloadAttachmentResultFileThenExceptionIsThrown() throws MalformedURLException, InfoLedgerEntityNotFoundException {
        // Given
        FileS3Info fileForAggregationInfo = mock(FileS3Info.class);
        when(fileForAggregationInfo.getBucketName()).thenReturn(BUCKET_NAME);
        when(fileForAggregationInfo.getFileKey()).thenReturn(FILE_KEY);
        URL url = new URL(FILE_URL);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(url);
        doThrow(new AmazonServiceException(CAN_NOT_LOAD_ENTITY_EXCEPTION_MESSAGE))
                .when(amazonS3).getObject(any(GetObjectRequest.class));

        // When
        S3Object attachmentFileS3Object = awsS3FileService.downloadFile(fileForAggregationInfo, false);

        // Then
        assertThat(attachmentFileS3Object).isNull();
    }
}
