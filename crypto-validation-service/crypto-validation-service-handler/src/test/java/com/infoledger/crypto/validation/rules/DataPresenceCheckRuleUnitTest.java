package com.infoledger.crypto.validation.rules;

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

class DataPresenceCheckRuleUnitTest {

  private final ValidationRule validationRule = new DataPresenceCheckRule();

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
        "fileCSVwithEmptyValue.csv",
        Map.of(
            "sheetCSV",
            new ValidationFailures("missedValues", "Excel sheet contains null or empty values")),
      },
      {
        "fileCSVwithMissedValue.csv",
        Map.of(
            "sheetCSV",
            new ValidationFailures(
                "missedColumns",
                "Excel sheet contains number of values in a row which does not fit number of columns.")),
      },
      {
        "fileCSVwithNoData.csv",
        Map.of(
            "sheetCSV",
            new ValidationFailures("dataMissed", "Excel sheet does not contain any data.")),
      },
      {
        "fileXlsWithEmptyValue.xls",
        Map.of(
            "fileCSVwithEmptyValue",
            new ValidationFailures(
                "missedColumns",
                "Excel sheet contains number of values in a row which does not fit number of columns.")),
      },
      {
        "fileXlsWithMissedValue.xls",
        Map.of(
            "fileCSVwithMissedValue",
            new ValidationFailures(
                "missedColumns",
                "Excel sheet contains number of values in a row which does not fit number of columns.")),
      },
      {
        "fileXlsWithNoData.xls",
        Map.of(
            "fileCSVwithNoData",
            new ValidationFailures("dataMissed", "Excel sheet does not contain any data.")),
      },
      {
        "fileXlsxWithEmptyValue.xlsx",
        Map.of(
            "fileCSVwithEmptyValue",
            new ValidationFailures(
                "missedColumns",
                "Excel sheet contains number of values in a row which does not fit number of columns.")),
      },
      {
        "fileXlsxWithMissedValue.xlsx",
        Map.of(
            "fileCSVwithMissedValue",
            new ValidationFailures(
                "missedColumns",
                "Excel sheet contains number of values in a row which does not fit number of columns.")),
      },
      {
        "fileXlsxWithNoData.xlsx",
        Map.of(
            "fileCSVwithNoData",
            new ValidationFailures("dataMissed", "Excel sheet does not contain any data.")),
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
