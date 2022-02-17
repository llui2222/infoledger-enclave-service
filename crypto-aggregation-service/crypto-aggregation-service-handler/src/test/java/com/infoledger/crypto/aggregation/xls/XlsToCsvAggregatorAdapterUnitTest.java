package com.infoledger.crypto.aggregation.xls;

import static com.infoledger.crypto.aggregation.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.infoledger.crypto.aggregation.csv.CsvByteArraysAggregator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class XlsToCsvAggregatorAdapterUnitTest {

  private static final byte[] EXISTING_DATA_NULL = null;

  private final CsvByteArraysAggregator aggregator = mock(CsvByteArraysAggregator.class);
  private final XlsToCsvAggregatorAdapter xlsToCsvAggregatorAdapter =
      new XlsToCsvAggregatorAdapter(aggregator);

  @Test
  void successfulAggregationWhenAggregatedDataNotExistYet() throws IOException, URISyntaxException {
    // given
    System.setProperty("line.separator", "\n");
    final byte[] newXlsDataOne = loadResource("xls-1-10-rows.xls");
    byte[] expected = loadResource("csv-merged-xls-1-result.csv");
    when(aggregator.aggregate(any(byte[].class), eq(EXISTING_DATA_NULL))).thenReturn(expected);

    // when
    byte[] result = xlsToCsvAggregatorAdapter.aggregate(newXlsDataOne, EXISTING_DATA_NULL);

    // then
    assertEquals(
        new String(expected, StandardCharsets.UTF_8), new String(result, StandardCharsets.UTF_8));
  }

  @Test
  void successfulAggregationWhenAggregatedDataExist() throws IOException, URISyntaxException {
    // given
    System.setProperty("line.separator", "\n");
    final byte[] newXlsDataOne = loadResource("xls-1-10-rows.xls");
    final byte[] existingData = loadResource("csv-1-10-rows.csv");
    byte[] expected = loadResource("csv-merged-xls-1-result.csv");
    when(aggregator.aggregate(any(byte[].class), any(byte[].class))).thenReturn(expected);

    // when
    byte[] result = xlsToCsvAggregatorAdapter.aggregate(newXlsDataOne, existingData);

    // then
    assertEquals(
        new String(expected, StandardCharsets.UTF_8), new String(result, StandardCharsets.UTF_8));
  }
}
