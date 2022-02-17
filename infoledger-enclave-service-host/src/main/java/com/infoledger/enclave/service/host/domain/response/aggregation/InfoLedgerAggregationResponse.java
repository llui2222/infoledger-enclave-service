package com.infoledger.enclave.service.host.domain.response.aggregation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoledger.enclave.service.host.domain.enums.Status;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import lombok.Getter;

import java.util.List;

@Getter
public class InfoLedgerAggregationResponse {

    private final Status status;
    private final FileS3Info aggregatedFileInfo;
    private final List<FileProcessingFailureReason> aggregationFailures;

    public InfoLedgerAggregationResponse(
            @JsonProperty("status") Status status,
            @JsonProperty("aggregatedFileInfo") FileS3Info aggregatedFileInfo,
            @JsonProperty("aggregationFailures") List<FileProcessingFailureReason> aggregationFailures) {
        this.status = status;
        this.aggregatedFileInfo = aggregatedFileInfo;
        this.aggregationFailures = aggregationFailures;
    }

    /**
     * Returns per-build OK response with the actual data content.
     *
     * @param aggregatedFileInfo File with aggregated data info.
     * @return OK response with Status=OK and aggregated data.
     */
    public static InfoLedgerAggregationResponse ok(FileS3Info aggregatedFileInfo) {
        return new InfoLedgerAggregationResponse(Status.OK, aggregatedFileInfo, null);
    }

    /**
     * Returns pre-build partially OK response with the failure specified.
     *
     * @param aggregatedFileInfo File with aggregated data info.
     * @param aggregationFailures List of files and aggregation Failure messages to provide to client.
     * @return OK response with failure messages for part of provided files.
     */
    public static InfoLedgerAggregationResponse partiallyOk(FileS3Info aggregatedFileInfo,
                                                            List<FileProcessingFailureReason> aggregationFailures) {
        return new InfoLedgerAggregationResponse(Status.OK, aggregatedFileInfo, aggregationFailures);
    }

    /**
     * Returns pre-build FAILED response with the failures specified.
     *
     * @param aggregationFailures List of files and aggregation Failure messages to provide to client.
     * @return FAILED response with failure message.
     */
    public static InfoLedgerAggregationResponse failed(List<FileProcessingFailureReason> aggregationFailures) {
        return new InfoLedgerAggregationResponse(Status.FAILED, null, aggregationFailures);
    }
}
