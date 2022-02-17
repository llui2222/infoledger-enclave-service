package com.infoledger.crypto.validation.rules;

import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_GET_SHEET_NAME;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FAILED_TO_CONVERT;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_CAN_NOT_BE_CONVERTED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_FORMAT_NOT_SUPPORTED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FORMAT_NOT_SUPPORTED;
import static com.infoledger.crypto.validation.utils.ValidationUtil.populateValidationFailures;

import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.validation.ValidationResult;
import com.infoledger.crypto.validation.ValidationRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/** Checks that data is convertible to well-formed excel file */
@Log4j2
public class ConvertibilityCheckRule implements ValidationRule {

  @Override
  public ValidationResult validate(byte[] data) {
    return validateIsConvertible(data);
  }

  /**
   * Validate that file created from incoming byte array has supported type and can be converted to
   * well-formed excel.
   *
   * @param data incoming file byte array
   * @return {@link ValidationResult}
   */
  private ValidationResult validateIsConvertible(byte[] data) {
    try (InputStream inputStream = new ByteArrayInputStream(data);
        InputStream is = FileMagic.prepareToCheckMagic(inputStream)) {

      FileMagic fileMagic = FileMagic.valueOf(is);
      log.debug("File type is: {}", fileMagic);

      if (FileMagic.UNKNOWN == fileMagic) {
        return isConvertibleCsv(inputStream);
      }

      if (FileMagic.OLE2 == fileMagic || FileMagic.OOXML == fileMagic) {
        return isConvertible(inputStream);
      }

      Map<String, ValidationFailures> validationFailures =
          populateValidationFailures(
              CAN_NOT_GET_SHEET_NAME,
              FORMAT_NOT_SUPPORTED,
              FILE_FORMAT_NOT_SUPPORTED + fileMagic.name());
      return ValidationResult.failed(validationFailures);
    } catch (Exception e) {
      log.debug("Can not convert file due to: {}", e.getMessage());
      Map<String, ValidationFailures> validationFailures =
          populateValidationFailures(
              CAN_NOT_GET_SHEET_NAME,
              FAILED_TO_CONVERT,
              FILE_CAN_NOT_BE_CONVERTED + e.getMessage());
      return ValidationResult.failed(validationFailures);
    }
  }

  /**
   * Verify that excel work book can be created from incoming file byte array input stream.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   * @throws IOException if fails
   */
  private ValidationResult isConvertible(InputStream inputStream) throws IOException {
    WorkbookFactory.create(inputStream);
    log.debug("File converted successfully.");
    return ValidationResult.ok();
  }

  /**
   * Verify that {@link CSVParser} can be created from incoming file byte array input stream.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   * @throws IOException if fails
   */
  private ValidationResult isConvertibleCsv(InputStream inputStream) throws IOException {
    try (Reader newCsvReader = new InputStreamReader(inputStream)) {
      CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(newCsvReader);
      log.debug("File converted successfully.");
      return ValidationResult.ok();
    }
  }
}
