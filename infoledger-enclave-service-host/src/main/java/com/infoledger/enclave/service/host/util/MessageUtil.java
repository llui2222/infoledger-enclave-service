package com.infoledger.enclave.service.host.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.infoledger.enclave.service.host.util.DateFormatUtil.getDateAsAFormattedString;

/**
 * Message util
 */
public final class MessageUtil {
    /**
     * Ctr.
     */
    private MessageUtil() {
    }

    /**
     * Message with timestamp
     *
     * @param message message
     * @return timestamped message
     */
    public static Map<String, Object> timestampedMessage(String message) {
        return new ImmutableMap.Builder<String, Object>()
                .put("timestamp", getDateAsAFormattedString(new Date()))
                .put("message", Strings.nullToEmpty(message))
                .build();
    }
}
