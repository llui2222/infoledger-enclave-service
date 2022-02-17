package com.infoledger.enclave.service.host.config;

import com.google.common.collect.ImmutableList;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.enclave.service.host.configuration.cognito.AwsCognitoIdTokenProcessor;
import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@TestConfiguration
public class TestConfig {

    private static final String TEST_ACCESS_KEY = "test-access-key";
    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String TEST_SESSION_TOKEN = "test-session-token";

    @Bean
    @Primary
    public AwsCognitoIdTokenProcessor awsCognitoIdTokenProcessor() {
        return new StubAwsCognitoIdTokenProcessor();
    }

    @Bean
    @Primary
    public EnclaveClient stubEnclaveClient() {
        return new StubEnclaveClient();
    }

    public static void mockSecurityContextHolderAuthentication() {
        User user = new User(TEST_ACCESS_KEY,
                TEST_SECRET_KEY,
                ImmutableList.of());
        JwtAuthentication jwtAuthentication = new JwtAuthentication(user,
                TEST_SESSION_TOKEN,
                null,
                null);

        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }
}