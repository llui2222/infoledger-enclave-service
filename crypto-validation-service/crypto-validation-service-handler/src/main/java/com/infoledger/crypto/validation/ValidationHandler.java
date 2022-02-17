package com.infoledger.crypto.validation;

import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_GET_SHEET_NAME;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FAILED_TO_DECRYPT;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_CAN_NOT_BE_DECRYPTED;

import com.infoledger.crypto.api.CryptoRequest;
import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.encryption.EncryptionApi;
import com.infoledger.crypto.handler.OperationHandler;
import com.infoledger.crypto.validation.api.ValidationRequest;
import com.infoledger.crypto.validation.api.ValidationResponse;
import com.infoledger.crypto.validation.rules.ColumnsMatchCheckRule;
import com.infoledger.crypto.validation.rules.ConvertibilityCheckRule;
import com.infoledger.crypto.validation.rules.DataPresenceCheckRule;
import com.infoledger.crypto.validation.validator.ChainedValidator;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ValidationHandler implements OperationHandler<ValidationRequest, ValidationResponse> {

  private static final String OPERATION_NAME = "validation";
  /** Validator iterating over validation rules until first failure; */
  private final ChainedValidator validator =
      new ChainedValidator(
          new ConvertibilityCheckRule(), new ColumnsMatchCheckRule(), new DataPresenceCheckRule());

  @Override
  public ValidationResponse handle(CryptoRequest cryptoRequest, EncryptionApi encryptor) {
    ValidationRequest request = (ValidationRequest) cryptoRequest;

    byte[] dataDecrypted;
    try {
      dataDecrypted = encryptor.decrypt(request.getData());

      log.debug("Successfully decrypted data");
    } catch (Exception e) {
      log.info("Failed to decrypt data from request : {}", e.getMessage());
      Map<String, ValidationFailures> validationFailures =
          populateValidationFailures(FAILED_TO_DECRYPT, FILE_CAN_NOT_BE_DECRYPTED + e.getMessage());
      return ValidationResponse.failed(validationFailures);
    }

    ValidationResult validationResult = validator.validate(dataDecrypted);
    log.debug("Validation result : {}", validationResult);

    if (validationResult.isValid()) {
      return ValidationResponse.ok();
    }

    return ValidationResponse.failed(validationResult.getValidationFailures());
  }

  @Override
  public Class<ValidationRequest> getHandledCryptoRequestType() {
    return ValidationRequest.class;
  }

  @Override
  public String getOperationName() {
    return OPERATION_NAME;
  }

  private Map<String, ValidationFailures> populateValidationFailures(
      String failureType, String failureMessage) {
    return Map.of(CAN_NOT_GET_SHEET_NAME, new ValidationFailures(failureType, failureMessage));
  }
}
