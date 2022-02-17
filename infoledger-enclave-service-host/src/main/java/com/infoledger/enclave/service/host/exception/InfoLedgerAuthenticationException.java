package com.infoledger.enclave.service.host.exception;

import javax.servlet.ServletException;

public class InfoLedgerAuthenticationException extends ServletException {
    public InfoLedgerAuthenticationException(String message) {
        super(message);
    }
}
