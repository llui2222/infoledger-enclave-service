package com.infoledger.enclave.service.host.controller.aggregation;

import com.infoledger.enclave.service.host.domain.request.aggregation.InfoLedgerAggregationRequest;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AggregationEnclaveClientService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
public class EnclaveServiceHostAggregationController {

    private final AggregationEnclaveClientService aggregationEnclaveClientService;

    public EnclaveServiceHostAggregationController(AggregationEnclaveClientService aggregationEnclaveClientService) {
        this.aggregationEnclaveClientService = aggregationEnclaveClientService;
    }

    @ApiOperation(httpMethod = "POST", value = "POST Aggregate data from attachments",
            notes = "POST Aggregate data from attachments",
            response = ResponseEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "POST Request Authorization header(firstly you should"
                    + " authenticate , then copy id token from auth response body and put it instead of 'id_token'" +
                    "in Authorization input)",
                    defaultValue = "Bearer=id_token", required = true, dataType = "string",
                    paramType = "header"),
            @ApiImplicitParam(name = "Content-Type", value = "POST Request Content type(needed value set as a default value," +
                    " do not change)",
                    defaultValue = "application/json", required = true, dataType = "string",
                    paramType = "header")
    })
    @ApiResponses(value = {
            @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, response = InfoLedgerAggregationResponse.class, message = "Data from message attachments aggregated"),
            @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, response = InfoLedgerAggregationResponse.class, message = "Aggregation for message attachments failed")})
    @PostMapping(value = "${infoledger.api.version}/aggregate",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<InfoLedgerAggregationResponse> aggregate(@ApiParam(value = "Required data for aggregation")
                                                                   @RequestBody InfoLedgerAggregationRequest request)
            throws InfoLedgerEntityNotFoundException,
            IOException {

        InfoLedgerAggregationResponse response = aggregationEnclaveClientService.aggregateAttachmentsData(request.getKmsKeyArn(),
                request.getAttachmentFilesS3Infos(),
                request.getFileResultS3Info());

        if (response.getAggregatedFileInfo() == null) {
            log.debug("Aggregation failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.debug("Data from message attachments aggregated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
