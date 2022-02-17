package com.infoledger.enclave.service.host.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd 'at' HH:mm:ss";

    /**
     * Ctr.
     */
    private DateFormatUtil() {
    }

    public static String getDateAsAFormattedString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }
}
