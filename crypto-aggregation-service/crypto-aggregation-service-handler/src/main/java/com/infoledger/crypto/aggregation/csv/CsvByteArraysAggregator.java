package com.infoledger.crypto.aggregation.csv;

import com.infoledger.crypto.aggregation.AggregatorApi;
import com.infoledger.crypto.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

/**
 * Aggregates 2 csv files represented as byte arrays.
 *
 * <p>Throws {@link CsvNotCompatibleException} if csv files have different headers (even
 * misordered).
 */
@Log4j2
public class CsvByteArraysAggregator implements AggregatorApi<byte[]> {

  @Override
  public byte[] aggregate(byte[] newData, byte[] existingData) {
    if (!Utils.nonEmpty(existingData)) {
      log.info("Existing data is empty, nothing to aggregate, returning new data file");
      return newData;
    }

    try (Reader newCsvReader = new InputStreamReader(new ByteArrayInputStream(newData));
        CSVParser newCsvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(newCsvReader);
        Reader existingCsvReader = new InputStreamReader(new ByteArrayInputStream(existingData));
        CSVParser existingCsvParser =
            CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(existingCsvReader);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(byteArrayOutputStream);
        CSVPrinter printer =
            new CSVPrinter(
                out,
                CSVFormat.DEFAULT
                    .withIgnoreEmptyLines()
                    .withSystemRecordSeparator()
                    .withHeader(existingCsvParser.getHeaderNames().toArray(new String[0])))) {

      if (!headersMatch(newCsvParser, existingCsvParser)) {
        log.info("Headers don't match, can't aggregate");
        throw new CsvNotCompatibleException(
            "Headers don't match thus can't merge new and existing csv files");
      }

      printer.printRecords(existingCsvParser.getRecords());
      printer.printRecords(newCsvParser.getRecords());
      out.flush();
      return byteArrayOutputStream.toByteArray();
    } catch (CsvNotCompatibleException e) {
      throw e;
    } catch (Exception e) {
      throw new CsvWriteException("Failed to write aggregated csv", e);
    }
  }

  private boolean headersMatch(CSVParser first, CSVParser second) {
    return first.getHeaderNames().equals(second.getHeaderNames());
  }
}
