package com.infoledger.enclave.service.host.controller.validation;

import com.infoledger.enclave.service.host.domain.request.validation.InfoLedgerValidationRequest;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.ValidationEnclaveClientService;
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
public class EnclaveServiceHostValidationController {

    private final ValidationEnclaveClientService validationEnclaveClientService;

    public EnclaveServiceHostValidationController(ValidationEnclaveClientService validationEnclaveClientService) {
        this.validationEnclaveClientService = validationEnclaveClientService;
    }

    @ApiOperation(httpMethod = "POST", value = "POST Validate data files from attachments",
            notes = "POST Validate data files attachments",
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
            @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, response = InfoLedgerValidationResponse.class, message = "Data from message attachments validated successfully"),
            @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, response = InfoLedgerValidationResponse.class, message = "Validation for message attachments failed")})
    @PostMapping(value = "${infoledger.api.version}/validate",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<InfoLedgerValidationResponse> validate(@ApiParam(value = "Required data for validation")
                                                                 @RequestBody InfoLedgerValidationRequest request)
            throws IOException, InfoLedgerEntityNotFoundException {

        InfoLedgerValidationResponse response = validationEnclaveClientService.validateAttachmentsData(request.getKmsKeyArn(),
                request.getAttachmentFilesS3Infos());

        if ((response.getValidationFailures() != null && !response.getValidationFailures().isEmpty()) || response.getValidationResultsPerFile().isEmpty()) {
            log.debug("Validation failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.debug("Data from message attachments validated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
