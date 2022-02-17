package com.infoledger.enclave.service.host.config;

import com.google.common.collect.ImmutableList;
import com.infoledger.enclave.service.host.configuration.cognito.AwsCognitoIdTokenProcessor;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import com.infoledger.enclave.service.host.exception.InfoLedgerAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Stub {@link AwsCognitoIdTokenProcessor} which authorizes access
 * for a specific test bearer without real interaction with cognito.
 */
public class StubAwsCognitoIdTokenProcessor implements AwsCognitoIdTokenProcessor {

    public static final String TEST_BEARER = "test-bearer";
    public static final String TEST_ACCESS_KEY = "test-access-key";
    public static final String TEST_SECRET_KEY = "test-secret-key";
    public static final String TEST_SESSION_TOKEN = "test-session-token";

    @Override
    public Authentication authenticate(HttpServletRequest request) throws InfoLedgerAuthenticationException {
        String authorizationHeader = request.getHeader("authorization");

        if  (!TEST_BEARER.equals(authorizationHeader)) {
            User user = new User(TEST_ACCESS_KEY,
                    TEST_SECRET_KEY,
                    ImmutableList.of());
            return new JwtAuthentication(user, TEST_SESSION_TOKEN, null, null);

        }

        throw new InfoLedgerAuthenticationException("Not Authorized");
    }
}
