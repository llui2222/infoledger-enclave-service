package com.infoledger.crypto.validation.utils;

import com.infoledger.crypto.api.ValidationFailures;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ValidationUtil {

  private ValidationUtil() {
    // Ctor. with no logic
  }

  public static List<String> getRowCellsValuesAsStringList(Row row) {
    List<String> rowCellsValues = new ArrayList<>();
    row.cellIterator().forEachRemaining(cell -> rowCellsValues.add(getCellValueAsString(cell)));
    return rowCellsValues;
  }

  public static List<String> getRowCellsValuesAsStringList(CSVRecord record) {
    List<String> rowCellsValues = new ArrayList<>();
    record.iterator().forEachRemaining(rowCellsValues::add);
    return rowCellsValues;
  }

  public static Map<String, ValidationFailures> populateValidationFailures(
      String sheetName, String failureType, String failureMessage) {
    return Map.of(sheetName, new ValidationFailures(failureType, failureMessage));
  }

  private static String getCellValueAsString(Cell cell) {
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        return String.valueOf(cell.getNumericCellValue());
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      default:
        return null;
    }
  }
}
