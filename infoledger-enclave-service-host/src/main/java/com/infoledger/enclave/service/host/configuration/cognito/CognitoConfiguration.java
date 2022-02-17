package com.infoledger.enclave.service.host.configuration.cognito;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Configuration
public class CognitoConfiguration {

    private final JwtConfiguration jwtConfiguration;

    public CognitoConfiguration(JwtConfiguration jwtConfiguration) {
        this.jwtConfiguration = jwtConfiguration;
    }

    @Bean
    public ConfigurableJWTProcessor<JWKSecurityContext> configurableJWTProcessor() throws MalformedURLException {
        ResourceRetriever resourceRetriever =
                new DefaultResourceRetriever(jwtConfiguration.getConnectionTimeout(),
                        jwtConfiguration.getReadTimeout());
        URL jwkSetURL = new URL(jwtConfiguration.getJwkUrl());
        JWKSource<JWKSecurityContext> keySource = new RemoteJWKSet<>(jwkSetURL, resourceRetriever);
        ConfigurableJWTProcessor<JWKSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<JWKSecurityContext> keySelector = new JWSVerificationKeySelector<>(RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        return jwtProcessor;
    }
}
