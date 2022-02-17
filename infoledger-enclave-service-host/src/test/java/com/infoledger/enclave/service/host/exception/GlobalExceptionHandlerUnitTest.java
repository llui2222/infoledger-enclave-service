package com.infoledger.enclave.service.host.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link GlobalExceptionHandler}
 */
class GlobalExceptionHandlerUnitTest {
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String MESSAGE_KEY = "message";
    private static final String ERROR_MESSAGE = "Internal Server Error";
    private static final String ENTITY_NOT_FOUND_ERROR_MESSAGE = "Message not found";
    private static final String AUTH_NOT_PASSED_ERROR_MESSAGE = "Auth not passed";
    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = null;

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleInfoLedgerEntityNotFoundException() {
        // Given
        InfoLedgerEntityNotFoundException exception = new InfoLedgerEntityNotFoundException(ENTITY_NOT_FOUND_ERROR_MESSAGE);

        // When
        Map<String, Object> timestampedMessage = exceptionHandler.handleException(exception);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, ENTITY_NOT_FOUND_ERROR_MESSAGE);
    }

    @Test
    void testHandleInfoLedgerAuthenticationExceptionn() {
        // Given
        InfoLedgerAuthenticationException exception = new InfoLedgerAuthenticationException(AUTH_NOT_PASSED_ERROR_MESSAGE);

        // When
        Map<String, Object> timestampedMessage = exceptionHandler.handleException(exception);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, AUTH_NOT_PASSED_ERROR_MESSAGE);
    }

    @Test
    void testHandleException() {
        // Given
        Exception exception = new Exception(ERROR_MESSAGE);

        // When
        Map<String, Object> timestampedMessage = exceptionHandler.handleException(exception);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, ERROR_MESSAGE);
    }

    @Test
    void testHandleExceptionWithNullMessage() {
        // Given
        Exception exception = new Exception(NULL_STRING);

        // When
        Map<String, Object> timestampedMessage = exceptionHandler.handleException(exception);

        // Then
        assertThat(timestampedMessage.get(TIMESTAMP_KEY)).isNotNull();
        assertThat(timestampedMessage).containsEntry(MESSAGE_KEY, EMPTY_STRING);
    }
}

