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

/** Unit test for {@link ConvertibilityCheckRule}. */
class ConvertibilityCheckRuleUnitTest {

  private final ValidationRule validationRule = new ConvertibilityCheckRule();

  @ParameterizedTest
  @ValueSource(
      strings = {"fileCSV.csv", "fileCSV.txt", "fileXLS.xls", "fileXLSX.xlsx", "loremIpsum.txt"})
  void validateThatFilesConvertibleIntoWellFormedExcelSupported(String input) throws IOException {
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
  void validateThatJpgFileNotSupported(String input, Map<String, ValidationFailures> expected)
      throws IOException {
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
        "image.jpg",
        Map.of(
            "canNotGetSheetName",
            new ValidationFailures("formatNotSupported", "File format not supported: JPEG")),
      },
      {
        "filePDF.pdf",
        Map.of(
            "canNotGetSheetName",
            new ValidationFailures("formatNotSupported", "File format not supported: PDF")),
      },
      {
        "fileXML.xml",
        Map.of(
            "canNotGetSheetName",
            new ValidationFailures("formatNotSupported", "File format not supported: XML")),
      },
      {
        "emptyTxt.txt",
        Map.of(
            "canNotGetSheetName",
            new ValidationFailures(
                "failedToConvert",
                "Can not convert file due to: The supplied file was empty (zero bytes long)")),
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
