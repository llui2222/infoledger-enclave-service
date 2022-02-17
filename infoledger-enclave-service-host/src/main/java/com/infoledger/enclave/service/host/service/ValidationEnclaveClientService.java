package com.infoledger.enclave.service.host.service;

import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;

import java.io.IOException;
import java.util.List;

public interface ValidationEnclaveClientService {

    InfoLedgerValidationResponse validateAttachmentsData(String kmsKeyArn,
                                                         List<FileS3Info> attachmentFilesInfos) throws InfoLedgerEntityNotFoundException,
            IOException;
}
