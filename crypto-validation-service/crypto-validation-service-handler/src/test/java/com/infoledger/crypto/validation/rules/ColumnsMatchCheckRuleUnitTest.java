package com.infoledger.crypto.validation.rules;

import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CAN_NOT_GET_SHEET_NAME;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.CSV_SHEET;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.EMPTY_SHEET;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.EXCEL_SHEET_IS_EMPTY;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.FILE_CAN_NOT_BE_VALIDATED;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.INCORRECT_ORDER_OF_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.MISSED_MANDATORY_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NOT_EXPECTED_COLUMNS;
import static com.infoledger.crypto.validation.ValidationFailureMessagesConstants.NOT_EXPECTED_EXTRA_COLUMNS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.infoledger.crypto.api.ValidationFailures;
import com.infoledger.crypto.validation.ValidationResult;
import com.infoledger.crypto.validation.ValidationRule;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnsMatchCheckRuleUnitTest {

  private static final String MISSED_COLUMNS_NAMES = "Some Id, Company Name, Input, Output";
  private static final String COLUMN_NAME_COMPANY_NAME = "Company Name";
  private static final String COLUMN_NAME_OUTPUT = "Output";
  private static final String COMPANY_NAME_HELLO = "Hello";
  private static final String COLUMN_NAMES_ORGANIZATION_NAME_AND_HELLO = "Organization Name, Hello";
  private static final String COLUMN_NAME_ORGANIZATION_NAME = "Organization Name";
  private static final String SHEET_NAME_FIL_22 = "fil22";

  private final ValidationRule validationRule = new ColumnsMatchCheckRule();

  @ParameterizedTest
  @ValueSource(strings = {"fileCSV.csv", "fileCSV.txt", "fileXLS.xls", "fileXLSX.xlsx"})
  void validatedSuccessfully(String input) throws IOException {
    // Given
    byte[] bytes = getFileBytes(input);

    // When
    ValidationResult validationResult = validationRule.validate(bytes);

    // Then
    ValidationResult expectedResult = ValidationResult.ok();
    assertEquals(validationResult, expectedResult);
  }

  @ParameterizedTest
  @MethodSource("validationParameters")
  void validationFails(String input, Map<String, ValidationFailures> expected) throws IOException {
    // Given
    byte[] bytes = getFileBytes(input);

    // When
    ValidationResult validationResult = validationRule.validate(bytes);

    // Then
    ValidationResult expectedResult = ValidationResult.failed(expected);
    assertEquals(validationResult, expectedResult);
  }

  public static Object[][] validationParameters() {
    return new Object[][] {
      {
        "loremIpsum.txt",
        Map.of(
            CSV_SHEET,
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS
                        + "Lorem ipsum dolor sit amet consectetur adipiscing...")
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + MISSED_COLUMNS_NAMES)),
      },
      {
        "incorrectOrderOfColumns.csv",
        Map.of(
            CSV_SHEET,
            new ValidationFailures(
                INCORRECT_ORDER_OF_COLUMNS,
                INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE + MISSED_COLUMNS_NAMES)),
      },
      {
        "lessColumnsThanRequired.csv",
        Map.of(
            CSV_SHEET,
            new ValidationFailures(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_OUTPUT)),
      },
      {
        "emptyTxt.txt",
        Map.of(
            CAN_NOT_GET_SHEET_NAME,
            new ValidationFailures(
                CAN_NOT_BE_VALIDATED,
                FILE_CAN_NOT_BE_VALIDATED + "The supplied file was empty (zero bytes long)")),
      },
      {
        "moreColumnsThanRequired.csv",
        Map.of(
            CSV_SHEET,
            new ValidationFailures(
                NOT_EXPECTED_COLUMNS, NOT_EXPECTED_EXTRA_COLUMNS + COMPANY_NAME_HELLO)),
      },
      {
        "partialMismatchBetweenColumnsNames.csv",
        Map.of(
            CSV_SHEET,
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS + COLUMN_NAME_ORGANIZATION_NAME)
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_COMPANY_NAME)),
      },
      {
        "incorrectOrderOfColumns.xls",
        Map.of(
            "incorrectOrderOfColumns",
            new ValidationFailures(
                INCORRECT_ORDER_OF_COLUMNS,
                INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE + MISSED_COLUMNS_NAMES)),
      },
      {
        "lessColumnsThanRequired.xls",
        Map.of(
            "lessColumnsThanRequired",
            new ValidationFailures(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_OUTPUT)),
      },
      {
        "moreColumnsThanRequired.xls",
        Map.of(
            "moreColumnsThanRequired",
            new ValidationFailures(
                NOT_EXPECTED_COLUMNS, NOT_EXPECTED_EXTRA_COLUMNS + COMPANY_NAME_HELLO)),
      },
      {
        "partialMismatchBetweenColumnsNames.xls",
        Map.of(
            "partialMismatchBetweenColumnsNames",
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS + COLUMN_NAME_ORGANIZATION_NAME)
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_COMPANY_NAME)),
      },
      {
        "fileWithOneEmptySheet.xls",
        Map.of(SHEET_NAME_FIL_22, new ValidationFailures(EMPTY_SHEET, EXCEL_SHEET_IS_EMPTY)),
      },
      {
        "incorrectOrderOfColumns.xlsx",
        Map.of(
            "incorrectOrderOfColumns",
            new ValidationFailures(
                INCORRECT_ORDER_OF_COLUMNS,
                INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE + MISSED_COLUMNS_NAMES)),
      },
      {
        "lessColumnsThanRequired.xlsx",
        Map.of(
            "lessColumnsThanRequired",
            new ValidationFailures(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_OUTPUT)),
      },
      {
        "moreColumnsThanRequired.xlsx",
        Map.of(
            "moreColumnsThanRequired",
            new ValidationFailures(
                NOT_EXPECTED_COLUMNS, NOT_EXPECTED_EXTRA_COLUMNS + COMPANY_NAME_HELLO)),
      },
      {
        "partialMismatchBetweenColumnsNames.xlsx",
        Map.of(
            "partialMismatchBetweenColumnsNa",
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS + COLUMN_NAME_ORGANIZATION_NAME)
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_COMPANY_NAME)),
      },
      {
        "fileWithOneEmptySheet.xlsx",
        Map.of(SHEET_NAME_FIL_22, new ValidationFailures(EMPTY_SHEET, EXCEL_SHEET_IS_EMPTY)),
      },
      {
        "emptyExcel.xls",
        Map.of("emptySheet", new ValidationFailures(EMPTY_SHEET, EXCEL_SHEET_IS_EMPTY)),
      },
      {
        "fileWithOneIncorrectSheet.xlsx",
        Map.of(
            SHEET_NAME_FIL_22,
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS + COLUMN_NAMES_ORGANIZATION_NAME_AND_HELLO)
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_COMPANY_NAME)),
      },
      {
        "fileWithSeveralIncorrectSheets.xlsx",
        Map.of(
            SHEET_NAME_FIL_22,
            new ValidationFailures(
                    NOT_EXPECTED_COLUMNS,
                    NOT_EXPECTED_EXTRA_COLUMNS + COLUMN_NAMES_ORGANIZATION_NAME_AND_HELLO)
                .addFailure(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_COMPANY_NAME),
            "fil23",
            new ValidationFailures(MISSED_COLUMNS, MISSED_MANDATORY_COLUMNS + COLUMN_NAME_OUTPUT),
            "fil24",
            new ValidationFailures(
                NOT_EXPECTED_COLUMNS, NOT_EXPECTED_EXTRA_COLUMNS + COMPANY_NAME_HELLO)),
      }
    };
  }

  private byte[] getFileBytes(String fileName) throws IOException {
    byte[] bytes;
    ClassLoader classLoader = getClass().getClassLoader();
    File file =
        new File(
            Objects.requireNonNull(classLoader.getResource("filesToValidate/" + fileName))
                .getFile());
    bytes = FileUtils.readFileToByteArray(file);
    return bytes;
  }
}
