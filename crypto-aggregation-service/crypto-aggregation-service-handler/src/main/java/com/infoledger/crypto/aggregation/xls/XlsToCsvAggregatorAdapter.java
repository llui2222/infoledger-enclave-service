package com.infoledger.crypto.aggregation.xls;

import com.infoledger.crypto.aggregation.AggregatorApi;
import com.infoledger.crypto.aggregation.csv.CsvByteArraysAggregator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@Log4j2
public class XlsToCsvAggregatorAdapter implements AggregatorApi<byte[]> {

  private final CsvByteArraysAggregator aggregator;

  public XlsToCsvAggregatorAdapter(CsvByteArraysAggregator aggregator) {
    this.aggregator = aggregator;
  }

  @Override
  public byte[] aggregate(byte[] newData, byte[] existingData) {
    try {
      byte[] newDataCsvBytes = convertXlsToCsvByteArray(newData);
      return aggregator.aggregate(newDataCsvBytes, existingData);
    } catch (Exception e) {
      log.warn("Failed to aggregate", e);

      throw new XlsAggregationException("Failed to aggregate xls documents", e);
    }
  }

  private static byte[] convertXlsToCsvByteArray(byte[] newData) throws IOException {
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

                    sb.append(getCellValueAsString(cell, workbook));
                  }
                }
              });

      String csvString = sb.toString();
      return csvString.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static String getCellValueAsString(Cell cell, Workbook workbook) {
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        return String.valueOf(cell.getNumericCellValue());
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        return getFormulaStringValue(cell, workbook);

      case ERROR:
      case BLANK:
      case _NONE:
      default:
        return null;
    }
  }

  private static String getFormulaStringValue(Cell cell, Workbook workbook) {
    log.debug("Formula is " + cell.getCellFormula());
    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    CellValue cellValue = evaluator.evaluate(cell);
    switch (cellValue.getCellType()) {
      case NUMERIC:
        return String.valueOf(cellValue.getNumberValue());
      case BOOLEAN:
        return String.valueOf(cellValue.getBooleanValue());
      case STRING:
        return cellValue.getStringValue();
      default:
        return null;
    }
  }
}
