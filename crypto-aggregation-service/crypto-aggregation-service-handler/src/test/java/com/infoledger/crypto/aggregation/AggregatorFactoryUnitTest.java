package com.infoledger.crypto.aggregation;

import static com.infoledger.crypto.aggregation.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.infoledger.crypto.aggregation.csv.CsvByteArraysAggregator;
import com.infoledger.crypto.aggregation.noop.NoOpByteArrayAggregator;
import com.infoledger.crypto.aggregation.xls.XlsToCsvAggregatorAdapter;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class AggregatorFactoryUnitTest {

  @Test
  void returnExpectedAggregatorForXLSFile() throws IOException, URISyntaxException {
    // Given
    final byte[] xlsData = loadResource("xls-1-10-rows.xls");

    // When
    AggregatorApi<byte[]> aggregatorApi = AggregatorFactory.forFileType(xlsData);

    // Then
    assertTrue(aggregatorApi instanceof XlsToCsvAggregatorAdapter);
  }

  @Test
  void returnExpectedAggregatorForCSVFile() throws IOException, URISyntaxException {
    // Given
    final byte[] csvData = loadResource("csv-1-10-rows.csv");

    // When
    AggregatorApi<byte[]> aggregatorApi = AggregatorFactory.forFileType(csvData);

    // Then
    assertTrue(aggregatorApi instanceof CsvByteArraysAggregator);
  }

  @Test
  void returnExpectedStubAggregatorForXMLFile() throws IOException, URISyntaxException {
    // Given
    final byte[] unsupportedData = loadResource("fileXML.xml");

    // When
    AggregatorApi<byte[]> aggregatorApi = AggregatorFactory.forFileType(unsupportedData);

    // Then
    assertTrue(aggregatorApi instanceof NoOpByteArrayAggregator);
  }
}
