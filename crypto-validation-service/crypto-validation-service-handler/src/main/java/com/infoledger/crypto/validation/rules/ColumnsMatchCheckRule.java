package com.infoledger.crypto.validation.rules;

import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_GET_SHEET_NAME;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.EMPTY_SHEET;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.EXCEL_SHEET_IS_EMPTY;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.INCORRECT_ORDER_OF_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_MANDATORY_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NOT_EXPECTED_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NOT_EXPECTED_EXTRA_COLUMNS;
import static com.infoledger.crypto.validation.utils.ValidationUtil.getRowCellsValuesAsStringList;
import static com.infoledger.crypto.validation.utils.ValidationUtil.populateValidationFailures;

import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.validation.ValidationResult;
import com.infoledger.crypto.validation.ValidationRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/** Validates that specific columns are present in the provided data. */
@Log4j2
public class ColumnsMatchCheckRule implements ValidationRule {

  // TODO: must be removed as soon as in future we will have real mandatory column names list
  private static final List<String> REQUIRED_COLUMNS_NAMES =
      Arrays.asList("Some Id", "Company Name", "Input", "Output");

  private static final String CSV_SHEET = "sheetCSV";
  private static final int COLUMN_NAME_SIZE_IN_MESSAGE_LIMIT = 50;

  @Override
  public ValidationResult validate(byte[] data) {
    return validateRequiredColumnsExistance(data);
  }

  /**
   * Validate that file created from incoming byte array has correct and expected columns names.
   *
   * @param data incoming file byte array
   * @return {@link ValidationResult}
   */
  private ValidationResult validateRequiredColumnsExistance(byte[] data) {
    try (InputStream inputStream = new ByteArrayInputStream(data);
        InputStream is = FileMagic.prepareToCheckMagic(inputStream)) {

      FileMagic fileMagic = FileMagic.valueOf(is);
      log.debug("File type is: {}", fileMagic);

      if (FileMagic.UNKNOWN == fileMagic) {
        return isRequiredColumnsPresentedInCsv(inputStream);
      }

      return isRequiredColumnsPresentedInExcel(inputStream);
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
   * Verify that excel work book created from incoming file byte array input stream has correct and
   * expected columns names.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   * @throws IOException if fails
   */
  private ValidationResult isRequiredColumnsPresentedInExcel(InputStream inputStream)
      throws IOException {
    Map<String, ValidationFailures> validationFailureResults = new HashMap<>();
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
      workbook
          .sheetIterator()
          .forEachRemaining(
              workbookSheet -> {
                Sheet sheetForValidation = workbook.getSheet(workbookSheet.getSheetName());
                if (sheetForValidation != null && sheetForValidation.getRow(0) != null) {
                  log.debug("Validating sheet with name {}", workbookSheet.getSheetName());
                  List<String> sheetCellsNames =
                      getRowCellsValuesAsStringList(sheetForValidation.getRow(0));
                  ValidationFailures validationFailuresMessages =
                      verifyIsAllRequiredColumnsArePresentedAndHaveCorrectOrder(
                          sheetCellsNames, REQUIRED_COLUMNS_NAMES);

                  if (!validationFailuresMessages.isEmpty()) {
                    validationFailureResults.put(
                        workbookSheet.getSheetName(), validationFailuresMessages);
                  }
                } else {
                  log.debug("Provided excel sheet is empty.");
                  ValidationFailures validationFailures =
                      new ValidationFailures(EMPTY_SHEET, EXCEL_SHEET_IS_EMPTY);
                  validationFailureResults.put(workbookSheet.getSheetName(), validationFailures);
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
   * Verify that csv created from incoming file byte array input stream has correct and expected
   * columns names.
   *
   * @param inputStream incoming file byte array input stream
   * @return {@link ValidationResult}
   * @throws IOException if fails
   */
  private ValidationResult isRequiredColumnsPresentedInCsv(InputStream inputStream)
      throws IOException {
    try (Reader newCsvReader = new InputStreamReader(inputStream);
        CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(newCsvReader)) {
      ValidationFailures validationFailures =
          verifyIsAllRequiredColumnsArePresentedAndHaveCorrectOrder(
              csvParser.getHeaderNames(), REQUIRED_COLUMNS_NAMES);
      if (validationFailures.isEmpty()) {
        return ValidationResult.ok();
      }

      Map<String, ValidationFailures> validationFailureResults =
          Map.of(CSV_SHEET, validationFailures);
      return ValidationResult.failed(validationFailureResults);
    }
  }

  private ValidationFailures verifyIsAllRequiredColumnsArePresentedAndHaveCorrectOrder(
      List<String> inputHeaderNames, List<String> requiredColumnsNames) {

    ValidationFailures validationFailures = new ValidationFailures();

    if (inputHeaderNames.equals(requiredColumnsNames)) {
      log.debug("All required columns presented in correct order.");
      return validationFailures;
    }

    List<String> missedColumns =
        verifyIsInputMissedMandatoryColumns(requiredColumnsNames, inputHeaderNames);
    if (!missedColumns.isEmpty()) {
      validationFailures.put(
          MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + buildStringValueFomList(missedColumns));
    }

    List<String> unexpectedExtraColumns =
        verifyIsInputHasUnexpectedExtraColumns(inputHeaderNames, requiredColumnsNames);
    if (!unexpectedExtraColumns.isEmpty()) {
      validationFailures.put(
          NOT_EXPECTED_COLUMNS,
          NOT_EXPECTED_EXTRA_COLUMNS + buildStringValueFomList(unexpectedExtraColumns));
    }

    if (missedColumns.isEmpty() && unexpectedExtraColumns.isEmpty()) {
      validationFailures.put(
          INCORRECT_ORDER_OF_COLUMNS,
          INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE
              + buildStringValueFomList(requiredColumnsNames));
    }

    log.debug("Columns names inside provided file does not fit required ones.");
    return validationFailures;
  }

  /**
   * Go through all provided excel column names and check if each column name size has allowed
   * number of characters. In case of exceeding allowed number of characters - cut column name to
   * fit size.
   *
   * @param values provided excel column names
   * @return coma separated string constructed from provided excel column names values.
   */
  private String buildStringValueFomList(List<String> values) {
    StringBuilder stringBuilder = new StringBuilder();
    Iterator<String> stringValues = values.iterator();
    while (stringValues.hasNext()) {
      String value = stringValues.next();
      if (value.length() > COLUMN_NAME_SIZE_IN_MESSAGE_LIMIT) {
        value = value.substring(0, COLUMN_NAME_SIZE_IN_MESSAGE_LIMIT - 1).trim() + "...";
      }
      stringBuilder.append(value);
      if (stringValues.hasNext()) {
        stringBuilder.append(", ");
      }
    }

    return stringBuilder.toString();
  }

  private List<String> verifyIsInputHasUnexpectedExtraColumns(
      List<String> headerNames, List<String> requiredColumnsNames) {
    return differenceBetweenColumnsNamesLists(headerNames, requiredColumnsNames);
  }

  private List<String> verifyIsInputMissedMandatoryColumns(
      List<String> requiredColumnsNames, List<String> headerNames) {
    return differenceBetweenColumnsNamesLists(requiredColumnsNames, headerNames);
  }

  private List<String> differenceBetweenColumnsNamesLists(
      List<String> columnNames, List<String> columnNamesToCompareWith) {
    return columnNames.stream()
        .filter(element -> !columnNamesToCompareWith.contains(element))
        .collect(Collectors.toList());
  }
}
