package com.infoledger.enclave.service.host.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.infoledger.enclave.service.host.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Global exception handler
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE
            = "Exception occurred while processing request: {}";

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link NullPointerException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(NullPointerException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link IllegalArgumentException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(IllegalArgumentException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link MethodArgumentNotValidException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(MethodArgumentNotValidException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link MethodArgumentTypeMismatchException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(MethodArgumentTypeMismatchException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link JsonMappingException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(JsonMappingException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle problem with processing request exception
     *
     * @param exception {@link MultipartException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleException(MultipartException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle entity not found info ledger exception
     *
     * @param exception {@link InfoLedgerEntityNotFoundException}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = NO_CONTENT)
    @ResponseBody
    public Map<String, Object> handleException(InfoLedgerEntityNotFoundException exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }

    /**
     * Handle unexpected info ledger exception
     *
     * @param exception {@link Exception}
     * @return error message
     */
    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleException(Exception exception) {
        log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_REQUEST_MESSAGE, exception.getMessage());
        return MessageUtil.timestampedMessage(exception.getMessage());
    }
}

