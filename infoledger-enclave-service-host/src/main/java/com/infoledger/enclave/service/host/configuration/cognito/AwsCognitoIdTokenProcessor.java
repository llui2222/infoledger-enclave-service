package com.infoledger.enclave.service.host.configuration.cognito;

import com.infoledger.enclave.service.host.exception.InfoLedgerAuthenticationException;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface AwsCognitoIdTokenProcessor {

    Authentication authenticate(HttpServletRequest request) throws InfoLedgerAuthenticationException;
}
