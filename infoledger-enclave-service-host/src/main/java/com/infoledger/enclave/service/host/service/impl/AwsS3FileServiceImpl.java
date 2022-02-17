package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * {@link AwsS3FileService} default implementation
 */
@Service
@Slf4j
public class AwsS3FileServiceImpl implements AwsS3FileService {

    private final AmazonS3 amazonS3;

    public AwsS3FileServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public FileS3Info storeFileProcessingResult(FileS3Info fileResultS3Info,
                                                byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            return null;
        }

        return uploadFile(fileResultS3Info, fileBytes);
    }

    @Override
    public S3Object downloadFile(FileS3Info fileS3Info, boolean isAttachment) throws InfoLedgerEntityNotFoundException {
        S3Object file = getFileFromBucket(fileS3Info.getBucketName(),
                fileS3Info.getFileKey());
        if (file == null) {
            if (isAttachment) {
                throw new InfoLedgerEntityNotFoundException("file not found");
            }
            return null;
        }
        return file;
    }

    /**
     * Download any file from s3
     *
     * @param bucket   S3 bucket name
     * @param fileName requested file name
     * @return requested file as resource
     */
    private S3Object getFileFromBucket(String bucket, String fileName) {
        S3Object object;
        try {
            URL s3Url = amazonS3.getUrl(bucket, fileName);
            log.info("Downloading file from s3: {}", s3Url.toExternalForm());
            object = amazonS3.getObject(new GetObjectRequest(bucket, fileName));
        } catch (AmazonServiceException e) {
            log.error("Failed to retrieve object {} from S3 Bucket {}", fileName, bucket);
            log.error(e.getErrorMessage());
            return null;
        }
        return object;
    }

    private FileS3Info uploadFile(FileS3Info fileResultS3Info,
                                  byte[] fileBytes) {
        log.info("File upload in progress.");
        FileS3Info result;
        try {
            result = uploadFileToS3Bucket(fileResultS3Info, fileBytes);
            log.info("File upload to S3 is completed.");
        } catch (AmazonServiceException ex) {
            log.info("File upload is failed.");
            return null;
        }
        return result;
    }

    private FileS3Info uploadFileToS3Bucket(FileS3Info fileResultS3Info,
                                            byte[] fileBytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);

        log.info("Uploading file with key = " + fileResultS3Info.getFileKey());

        PutObjectRequest putObjectRequest = new PutObjectRequest(fileResultS3Info.getBucketName(),
                fileResultS3Info.getFileKey(),
                new ByteArrayInputStream(fileBytes),
                metadata);

        amazonS3.putObject(putObjectRequest);

        return fileResultS3Info;
    }
}
