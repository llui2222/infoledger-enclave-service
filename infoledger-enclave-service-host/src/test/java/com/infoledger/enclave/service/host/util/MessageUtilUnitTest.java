package com.infoledger.enclave.service.host.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.infoledger.enclave.service.host.util.MessageUtil.timestampedMessage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MessageUtil}
 */
class MessageUtilUnitTest {
    private static final String MESSAGE = "Message";
    private static final String NULL_VALUE = null;
    private static final String EMPTY_MESSAGE = "";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String MESSAGE_KEY = "message";

    @Test
    void testCanCreateTimeStampedMessage() {
        // When
        Map<String, Object> timestampedMessage = timestampedMessage(MESSAGE);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, MESSAGE);
    }

    @Test
    void testCanCreateTimeStampedMessageForNullInput() {
        // When
        Map<String, Object> timestampedMessage = timestampedMessage(NULL_VALUE);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, EMPTY_MESSAGE);
    }
}
