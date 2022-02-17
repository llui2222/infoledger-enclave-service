package com.infoledger.crypto.aggregation;

import com.infoledger.crypto.aggregation.csv.CsvByteArraysAggregator;
import com.infoledger.crypto.aggregation.noop.NoOpByteArrayAggregator;
import com.infoledger.crypto.aggregation.xls.XlsToCsvAggregatorAdapter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.poifs.filesystem.FileMagic;

/**
 * Provides proper {@link AggregatorApi} with respect to incoming request - it can be, for example,
 * csv aggregator,or archives aggregator, etc.
 */
public class AggregatorFactory {

  private static final CsvByteArraysAggregator CSV_BYTE_ARRAY_AGGREGATOR =
      new CsvByteArraysAggregator();
  private static final XlsToCsvAggregatorAdapter XLS_BYTE_ARRAYS_AGGREGATOR =
      new XlsToCsvAggregatorAdapter(CSV_BYTE_ARRAY_AGGREGATOR);
  private static final NoOpByteArrayAggregator NO_OP_BYTE_ARRAY_AGGREGATOR =
      new NoOpByteArrayAggregator();

  private AggregatorFactory() {
    // empty ctor.
  }

  private enum FileType {
    CSV,
    XLS,
    UNSUPPORTED
  }

  public static AggregatorApi<byte[]> forFileType(byte[] excelFileByteArray) throws IOException {
    switch (getExcelFileType(excelFileByteArray)) {
      case CSV:
        return CSV_BYTE_ARRAY_AGGREGATOR;

      case XLS:
        return XLS_BYTE_ARRAYS_AGGREGATOR;

      default:
        return NO_OP_BYTE_ARRAY_AGGREGATOR;
    }
  }

  private static FileType getExcelFileType(byte[] newDataDecrypted) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(newDataDecrypted);
        InputStream is = FileMagic.prepareToCheckMagic(inputStream)) {

      FileMagic fileMagic = FileMagic.valueOf(is);

      if (FileMagic.OLE2 == fileMagic || FileMagic.OOXML == fileMagic) {
        return FileType.XLS;
      }

      if (FileMagic.UNKNOWN == fileMagic) {
        return FileType.CSV;
      }

      return FileType.UNSUPPORTED;
    }
  }
}
