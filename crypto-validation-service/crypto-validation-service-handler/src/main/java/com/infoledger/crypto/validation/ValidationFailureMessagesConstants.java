package com.infoledger.crypto.validation;

public class ValidationFailureMessagesConstants {

  private ValidationFailureMessagesConstants() {
    // Private ctor., no logic needed here
  }

  public static final String FAILED_TO_DECRYPT = "failedToDecrypt";
  public static final String FILE_CAN_NOT_BE_DECRYPTED = "Failed to decrypt data from request: ";
  public static final String FAILED_TO_CONVERT = "failedToConvert";
  public static final String FORMAT_NOT_SUPPORTED = "formatNotSupported";
  public static final String FILE_FORMAT_NOT_SUPPORTED = "File format not supported: ";
  public static final String FILE_CAN_NOT_BE_CONVERTED = "Can not convert file due to: ";
  public static final String CAN_NOT_GET_SHEET_NAME = "canNotGetSheetName";
  public static final String CAN_NOT_BE_VALIDATED = "fileCanNotBeValidated";
  public static final String FILE_CAN_NOT_BE_VALIDATED = "Can not validate file due to: ";

  public static final String CSV_SHEET = "sheetCSV";
  public static final String EMPTY_SHEET = "emptyExcelSheet";
  public static final String EXCEL_SHEET_IS_EMPTY = "Excel sheet is empty.";
  public static final String DATA_MISSED = "dataMissed";
  public static final String EXCEL_SHEET_DOES_NOT_CONTAIN_DATA =
      "Excel sheet does not contain any data.";
  public static final String NUMBER_OF_COLUMNS_AND_NUMBER_OF_VALUES_DOES_NOT_FIT =
      "Excel sheet contains number of values in a row which does not fit number of columns.";
  public static final String MISSED_VALUES = "missedValues";
  public static final String MISSED_COLUMNS = "missedColumns";
  public static final String MISSED_MANDATORY_COLUMNS = "Following mandatory columns are missed: ";
  public static final String NOT_EXPECTED_COLUMNS = "notExpectedColumns";
  public static final String NOT_EXPECTED_EXTRA_COLUMNS =
      "Following columns are not stated in the template: ";
  public static final String NULL_OR_EMPTY_VALUES = "Excel sheet contains null or empty values";
  public static final String INCORRECT_ORDER_OF_COLUMNS = "incorrectColumnsOrder";
  public static final String INCORRECT_ORDER_OF_COLUMNS_AGAINST_TEMPLATE =
      "Columns in provided file have incorrect order "
          + "according to template. Order of columns must be following: ";
}
