package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.aggregation.enclave.client.EnclaveValidationException;
import com.infoledger.crypto.api.CryptoResponse;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.validation.InfoLedgerValidationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
import com.infoledger.enclave.service.host.service.ValidationEnclaveClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ValidationEnclaveClientService} default implementation
 */
@Service
@Slf4j
@SuppressWarnings({"squid:S5042"})
public class ValidationEnclaveClientServiceImpl implements ValidationEnclaveClientService {

  private final AwsS3FileService awsS3FileService;
  private final EnclaveClient enclaveClient;

  public ValidationEnclaveClientServiceImpl(AwsS3FileService awsS3FileService, EnclaveClient enclaveClient) {
    this.awsS3FileService = awsS3FileService;
    this.enclaveClient = enclaveClient;
  }

  @Override
  public InfoLedgerValidationResponse validateAttachmentsData(String kmsKeyArn,
                                                              List<FileS3Info> attachmentFilesInfos)
      throws InfoLedgerEntityNotFoundException, IOException {
    JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    User principal = authentication.getPrincipal();
    AWSCredentials awsCredentials = new BasicSessionCredentials(principal.getUsername(),
        principal.getPassword(),
        authentication.getSessionToken());

    List<FileProcessingFailureReason> errorsList = new ArrayList<>();
    Map<String, ValidationResponse> validationResultsPerFile = new HashMap<>();

    for (FileS3Info attachmentFileInfo : attachmentFilesInfos) {
      S3Object attachmentFile = awsS3FileService.downloadFile(attachmentFileInfo, true);

      try (InputStream attachmentInputStream = attachmentFile.getObjectContent();
           ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        attachmentInputStream.transferTo(output);
        byte[] fileBytes = output.toByteArray();
        String entryName = attachmentFile.getKey();
        log.debug("Processing file " + entryName);
        try {
          ValidationResponse response = enclaveClient.validate(awsCredentials,
              kmsKeyArn,
              fileBytes);
          log.debug("Validated successfully: " + entryName);
          validationResultsPerFile.put(entryName, response);
        } catch (EnclaveValidationException ex) {
          log.debug("Validation failed for: " + entryName);
          FileProcessingFailureReason fileValidationFailureReason = new FileProcessingFailureReason(entryName,
              ex.getMessage());
          errorsList.add(fileValidationFailureReason);
        }
      }
    }

    return buildResponse(validationResultsPerFile, errorsList);
  }

  private InfoLedgerValidationResponse buildResponse(Map<String, ValidationResponse> validationResultsPerFile,
                                                     List<FileProcessingFailureReason> errorsList) {
    if (!errorsList.isEmpty() || validationResultsPerFile.values().stream()
        .anyMatch(validationResponse -> CryptoResponse.Status.FAILED == validationResponse.getStatus())) {
      return InfoLedgerValidationResponse.failed(validationResultsPerFile, errorsList);
    }

    return InfoLedgerValidationResponse.ok(validationResultsPerFile);
  }
}
