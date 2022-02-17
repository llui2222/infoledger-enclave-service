package com.infoledger.crypto.aggregation.csv;

/**
 * Exception which indicates that 2 csv files which are attempted to be merged, are actually not
 * compatible.
 */
public class CsvNotCompatibleException extends RuntimeException {

  public CsvNotCompatibleException(String message) {
    super(message);
  }
}
