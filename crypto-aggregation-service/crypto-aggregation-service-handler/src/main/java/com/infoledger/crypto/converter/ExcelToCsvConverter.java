package com.infoledger.crypto.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelToCsvConverter {

  private ExcelToCsvConverter() {
    // empty ctor.
  }

  public static byte[] convertExcelToCsv(byte[] newData) throws IOException {
    return convertExcelToCsvByteArray(newData);
  }

  private static byte[] convertExcelToCsvByteArray(byte[] newData) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (InputStream inputStream = new ByteArrayInputStream(newData);
        Workbook workbook = WorkbookFactory.create(inputStream)) {
      AtomicInteger sheetNumber = new AtomicInteger(0);
      workbook
          .sheetIterator()
          .forEachRemaining(
              sheetForValidation -> {
                // Iterate through all the rows in the selected sheet
                Iterator<Row> rowIterator = sheetForValidation.rowIterator();
                if (sheetNumber.getAndIncrement() > 0) {
                  rowIterator.next();
                }
                while (rowIterator.hasNext()) {
                  if (sb.length() != 0) {
                    sb.append("\n");
                  }
                  Row row = rowIterator.next();

                  Iterator<Cell> cellIterator = row.cellIterator();
                  while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (sb.length() != 0 && !sb.toString().endsWith("\n")) {
                      sb.append(",");
                    }

                    switch (cell.getCellType()) {
                      case STRING:
                        sb.append(cell.getStringCellValue());
                        break;
                      case NUMERIC:
                        sb.append(cell.getNumericCellValue());
                        break;
                      case BOOLEAN:
                        sb.append(cell.getBooleanCellValue());
                        break;
                      default:
                    }
                  }
                }
              });

      String csvString = sb.toString();
      return csvString.getBytes(StandardCharsets.UTF_8);
    }
  }
}
