package com.infoledger.crypto.aggregation.csv;

import static com.infoledger.crypto.aggregation.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CsvByteArraysAggregatorUnitTest {

  private final CsvByteArraysAggregator aggregator = new CsvByteArraysAggregator();

  @Test
  void successfulAggregation() throws IOException, URISyntaxException {
    // given
    System.setProperty("line.separator", "\n");
    final byte[] newCsv = loadResource("csv-2.csv");
    final byte[] existingCsv = loadResource("csv-1.csv");

    // when
    byte[] result = aggregator.aggregate(newCsv, existingCsv);

    // then
    byte[] expected = loadResource("csv-1-2-result.csv");
    assertEquals(
        new String(expected, StandardCharsets.UTF_8), new String(result, StandardCharsets.UTF_8));
  }

  @Test
  void successfulAggregationWithNull() throws IOException, URISyntaxException {
    // given
    final byte[] newCsv = loadResource("csv-2.csv");
    final byte[] existingCsv = null;

    // when
    byte[] result = aggregator.aggregate(newCsv, existingCsv);

    // then
    byte[] expected = loadResource("csv-2.csv");
    assertEquals(
        new String(expected, StandardCharsets.UTF_8), new String(result, StandardCharsets.UTF_8));
  }

  @Test
  void successfulAggregationWithEmpty() throws IOException, URISyntaxException {
    // given
    final byte[] newCsv = loadResource("csv-2.csv");
    final byte[] existingCsv = new byte[0];

    // when
    byte[] result = aggregator.aggregate(newCsv, existingCsv);

    // then
    byte[] expected = loadResource("csv-2.csv");
    assertEquals(
        new String(result, StandardCharsets.UTF_8), new String(expected, StandardCharsets.UTF_8));
  }

  @Test
  void aggregationOfIncompatibleFails() throws IOException, URISyntaxException {
    // given
    final byte[] newCsv = loadResource("csv-incompatible.csv");
    final byte[] existingCsv = loadResource("csv-1.csv");

    // when & then
    assertThrows(CsvNotCompatibleException.class, () -> aggregator.aggregate(newCsv, existingCsv));
  }

  @Test
  void aggregationOfNotReadableCsvFails() throws IOException, URISyntaxException {
    // given
    final byte[] newCsv = loadResource("csv-not-readable.csv");
    final byte[] existingCsv = loadResource("csv-1.csv");

    // when & then
    assertThrows(CsvNotCompatibleException.class, () -> aggregator.aggregate(newCsv, existingCsv));
  }

  @Test
  void aggregationOfNotReadableCsvFails1() throws IOException, URISyntaxException {
    // given
    final byte[] newCsv = null;
    final byte[] existingCsv = loadResource("csv-2.csv");

    // when & then
    assertThrows(CsvWriteException.class, () -> aggregator.aggregate(newCsv, existingCsv));
  }
}
