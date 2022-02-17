package com.infoledger.crypto.aggregation.csv;

/** Indicates about any issues occurred while writing out the aggregated csv. */
public class CsvWriteException extends RuntimeException {

  public CsvWriteException(String message, Throwable cause) {
    super(message, cause);
  }
}
