package com.infoledger.enclave.service.host.service;

import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;

/**
 * AwsS3 file service method definitions
 */
public interface AwsS3FileService {
    FileS3Info storeFileProcessingResult(FileS3Info fileResultS3Info,
                                         byte[] fileBytes);

    S3Object downloadFile(FileS3Info fileS3Info, boolean isAttachment) throws InfoLedgerEntityNotFoundException;
}
