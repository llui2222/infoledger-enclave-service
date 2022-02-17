package com.infoledger.crypto.validation.rules;

import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_GET_SHEET_NAME;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CSV_SHEET;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.DATA_MISSED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.EXCEL_SHEET_DOES_NOT_CONTAIN_DATA;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_VALUES;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NULL_OR_EMPTY_VALUES;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NUMBER_OF_COLUMNS_AND_NUMBER_OF_VALUES_DOES_NOT_FIT;
import static com.infoledger.crypto.validation.utils.ValidationUtil.getRowCellsValuesAsStringList;
import static com.infoledger.crypto.validation.utils.ValidationUtil.populateValidationFailures;

import com.amazonaws.util.StringUtils;
import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.validation.ValidationResult;
import com.infoledger.crypto.validation.ValidationRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/** Validates that the file contains all required data. */
@Log4j2
public class DataPresenceCheckRule implements ValidationRule {

  @Override
  public ValidationResult validate(byte[] data) {
    return validateDataExistance(data);
  }

  /**
   * Validate that file created from incoming byte array contains data.
   *
   * @param data incoming file byte array
   * @return {@link ValidationResult}
   */
  private ValidationResult validateDataExistance(byte[] data) {
    try (InputStream inputStream = new ByteArrayInputStream(data);
        InputStream is = FileMagic.prepareToCheckMagic(inputStream)) {

      FileMagic fileMagic = FileMagic.valueOf(is);
      log.debug("File type is: {}", fileMagic);

      if (FileMagic.UNKNOWN == fileMagic) {
        return isDataPresentedCsv(inputStream);
      }

      return isDataPresented(inputStream);
    } catch (Exception e) {
      log.debug("Can not validate file due to: {}", e.getMessage());
      Map<String, ValidationFailures> validationFailures =
          populateValidationFailures(
              CAN_NOT_GET_SHEET_NAME,
              CAN_NOT_BE_VALIDATED,
              FILE_CAN_NOT_BE_VALIDATED + e.getMessage());
      return ValidationResult.failed(validationFailures);
    }
  }

  /**
   * Verify that excel work book created from incoming file byte array input stream contains data.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   * @throws IOException if fails
   */
  private ValidationResult isDataPresented(InputStream inputStream) throws IOException {
    Map<String, ValidationFailures> validationFailureResults = new HashMap<>();
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
      workbook
          .sheetIterator()
          .forEachRemaining(
              workbookSheet -> {
                Sheet sheetForValidation = workbook.getSheet(workbookSheet.getSheetName());
                log.debug("Validating sheet with name {}", workbookSheet.getSheetName());
                int rowsNumber = sheetForValidation.getPhysicalNumberOfRows();
                if (rowsNumber > 1) {
                  int numberColumns = sheetForValidation.getRow(0).getPhysicalNumberOfCells();
                  ValidationFailures validationFailuresMessages;
                  for (Row row : sheetForValidation) {
                    List<String> rowCellsValues = getRowCellsValuesAsStringList(row);

                    validationFailuresMessages =
                        verifyIsDataPresented(rowCellsValues, numberColumns);

                    if (!validationFailuresMessages.isEmpty()) {
                      validationFailureResults.put(
                          workbookSheet.getSheetName(), validationFailuresMessages);
                    }
                  }
                } else {
                  log.debug("Provided excel sheet does not contain data.");
                  validationFailureResults.putAll(
                      populateValidationFailures(
                          workbookSheet.getSheetName(),
                          DATA_MISSED,
                          EXCEL_SHEET_DOES_NOT_CONTAIN_DATA));
                }
              });

      if (validationFailureResults.isEmpty()) {
        log.debug("File validated successfully.");
        return ValidationResult.ok();
      }

      return ValidationResult.failed(validationFailureResults);
    }
  }

  /**
   * Verify that csv created from incoming file byte array input stream contains data.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   */
  private ValidationResult isDataPresentedCsv(InputStream inputStream) throws IOException {
    try (Reader newCsvReader = new InputStreamReader(inputStream);
        CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(newCsvReader)) {
      int numberOfColumns = csvParser.getHeaderNames().size();
      List<CSVRecord> csvRecords = csvParser.getRecords();
      if (csvRecords.isEmpty()) {
        log.debug("Provided excel sheet does not contain data.");
        Map<String, ValidationFailures> validationFailures =
            populateValidationFailures(CSV_SHEET, DATA_MISSED, EXCEL_SHEET_DOES_NOT_CONTAIN_DATA);
        return ValidationResult.failed(validationFailures);
      }
      ValidationFailures validationFailures;
      for (CSVRecord record : csvRecords) {
        List<String> rowColumnsValues = getRowCellsValuesAsStringList(record);

        validationFailures = verifyIsDataPresented(rowColumnsValues, numberOfColumns);

        if (!validationFailures.isEmpty()) {
          Map<String, ValidationFailures> validationFailureResults =
              Map.of(CSV_SHEET, validationFailures);
          return ValidationResult.failed(validationFailureResults);
        }
      }

      return ValidationResult.ok();
    }
  }

  private ValidationFailures verifyIsDataPresented(
      List<String> inputRowValues, int numberOfColumns) {

    ValidationFailures validationFailures = new ValidationFailures();

    boolean isNullOrEmptyValuePresented = false;
    if (inputRowValues.size() == numberOfColumns) {
      log.debug("Correct data provided in each cell.");
      isNullOrEmptyValuePresented = inputRowValues.stream().anyMatch(StringUtils::isNullOrEmpty);
    } else {
      log.debug("Number of provided values does not fit number of columns.");
      validationFailures.put(MISSED_COLUMNS, NUMBER_OF_COLUMNS_AND_NUMBER_OF_VALUES_DOES_NOT_FIT);
    }

    if (isNullOrEmptyValuePresented) {
      validationFailures.put(MISSED_VALUES, NULL_OR_EMPTY_VALUES);
    }

    return validationFailures;
  }
}
