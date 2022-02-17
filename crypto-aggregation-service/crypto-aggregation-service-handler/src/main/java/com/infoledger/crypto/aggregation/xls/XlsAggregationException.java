package com.infoledger.crypto.aggregation.xls;

/** Exception indicating aggregation failure. */
public class XlsAggregationException extends RuntimeException {

  public XlsAggregationException(String message, Throwable cause) {
    super(message, cause);
  }
}
