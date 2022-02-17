package com.infoledger.crypto.coverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.infoledger.crypto.converter.ExcelToCsvConverter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class ExcelToCsvConverterUnitTest {
  @Test
  void givenCorrectXlsxFileWhenConvertToCsvThenResultCsvContainsExpectedData() throws IOException {
    // Given
    System.setProperty("line.separator", "\n");
    byte[] fileBytesBeforeConversion = getFileBytes("fileXLSXWithSeveralSheets.xlsx");

    // When
    byte[] bytesAfterConversion = ExcelToCsvConverter.convertExcelToCsv(fileBytesBeforeConversion);

    // Then
    byte[] expectedBytesAfterConversion = getFileBytes("expected_csv.csv");
    assertEquals(
        new String(expectedBytesAfterConversion, StandardCharsets.UTF_8),
        new String(bytesAfterConversion, StandardCharsets.UTF_8));
  }

  private byte[] getFileBytes(String fileName) throws IOException {
    byte[] bytes;
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
    bytes = FileUtils.readFileToByteArray(file);
    return bytes;
  }
}
