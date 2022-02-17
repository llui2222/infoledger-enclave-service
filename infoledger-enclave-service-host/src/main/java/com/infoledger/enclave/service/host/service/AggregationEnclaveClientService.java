package com.infoledger.enclave.service.host.service;

import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;

import java.io.IOException;
import java.util.List;

public interface AggregationEnclaveClientService {

    InfoLedgerAggregationResponse aggregateAttachmentsData(String kmsKeyArn,
                                                           List<FileS3Info> attachmentFilesInfos,
                                                           FileS3Info aggregationResultFileInfo) throws InfoLedgerEntityNotFoundException,
            IOException;
}
