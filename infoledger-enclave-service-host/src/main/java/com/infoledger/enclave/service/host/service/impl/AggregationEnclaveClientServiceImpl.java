package com.infoledger.enclave.service.host.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.model.S3Object;
import com.infoledger.aggregation.enclave.client.EnclaveAggregationException;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import com.infoledger.enclave.service.host.domain.error.FileProcessingFailureReason;
import com.infoledger.enclave.service.host.domain.request.FileS3Info;
import com.infoledger.enclave.service.host.domain.response.aggregation.InfoLedgerAggregationResponse;
import com.infoledger.enclave.service.host.exception.InfoLedgerEntityNotFoundException;
import com.infoledger.enclave.service.host.service.AggregationEnclaveClientService;
import com.infoledger.enclave.service.host.service.AwsS3FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link AggregationEnclaveClientService} default implementation
 */
@Service
@Slf4j
@SuppressWarnings({"squid:S5042"})
public class AggregationEnclaveClientServiceImpl implements AggregationEnclaveClientService {

  private final AwsS3FileService awsS3FileService;
  private final EnclaveClient enclaveClient;

  public AggregationEnclaveClientServiceImpl(AwsS3FileService awsS3FileService, EnclaveClient enclaveClient) {
    this.awsS3FileService = awsS3FileService;
    this.enclaveClient = enclaveClient;
  }

  @Override
  public InfoLedgerAggregationResponse aggregateAttachmentsData(String kmsKeyArn,
                                                                List<FileS3Info> attachmentFilesInfos,
                                                                FileS3Info aggregationResultFileInfo) throws InfoLedgerEntityNotFoundException,
      IOException {
    S3Object aggregationResultFile = awsS3FileService.downloadFile(aggregationResultFileInfo, false);
    byte[] aggregationResult = null;
    if (aggregationResultFile != null) {
      try (InputStream aggregationResultFileInputStream = aggregationResultFile.getObjectContent();
           ByteArrayOutputStream outputResult = new ByteArrayOutputStream()) {
        log.debug("Processing aggregation result file.");

        aggregationResultFileInputStream.transferTo(outputResult);
        aggregationResult = outputResult.toByteArray();
      }
    }

    JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    User principal = authentication.getPrincipal();
    AWSCredentials awsCredentials = new BasicSessionCredentials(principal.getUsername(),
        principal.getPassword(),
        authentication.getSessionToken());

    List<FileProcessingFailureReason> errorsList = new ArrayList<>();

    for (FileS3Info attachmentFileInfo : attachmentFilesInfos) {
      S3Object attachmentFile = awsS3FileService.downloadFile(attachmentFileInfo, true);

      try (InputStream attachmentInputStream = attachmentFile.getObjectContent();
           ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        attachmentInputStream.transferTo(output);
        byte[] fileBytes = output.toByteArray();
        String fileName = attachmentFile.getKey();
        log.debug("Processing file " + fileName);
        try {
          aggregationResult = enclaveClient.aggregate(awsCredentials,
              kmsKeyArn,
              fileBytes,
              aggregationResult);
          log.debug("Aggregated successfully: " + fileName);
        } catch (EnclaveAggregationException ex) {
          log.debug("Aggregation failed for: " + fileName);
          FileProcessingFailureReason fileAggregationFailureReason = new FileProcessingFailureReason(fileName,
              ex.getMessage());
          errorsList.add(fileAggregationFailureReason);
        }
      }
    }

    if (!errorsList.isEmpty()) {
      log.warn("Aggregation was not done.");
      return InfoLedgerAggregationResponse.failed(errorsList);
    }

    FileS3Info aggregatedFileInfo = awsS3FileService.storeFileProcessingResult(aggregationResultFileInfo, aggregationResult);
    log.debug("Aggregation succeed.");
    return InfoLedgerAggregationResponse.ok(aggregatedFileInfo);
  }
}
