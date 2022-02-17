package com.infoledger.enclave.service.host.configuration.cognito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.infoledger.enclave.service.host.exception.InfoLedgerAuthenticationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.Credentials;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;

@Component
@Slf4j
public class AwsCognitoIdTokenProcessorImpl implements AwsCognitoIdTokenProcessor {

    private final JwtConfiguration jwtConfiguration;
    private final ConfigurableJWTProcessor<JWKSecurityContext> configurableJWTProcessor;

    public AwsCognitoIdTokenProcessorImpl(JwtConfiguration jwtConfiguration,
                                          ConfigurableJWTProcessor<JWKSecurityContext> configurableJWTProcessor) {
        this.jwtConfiguration = jwtConfiguration;
        this.configurableJWTProcessor = configurableJWTProcessor;
    }

    @Override
    public Authentication authenticate(HttpServletRequest request) throws InfoLedgerAuthenticationException {
        String authHeader = request.getHeader(jwtConfiguration.getHttpHeader());
        if (authHeader != null) {
            String idToken = getBearerToken(authHeader);
            JWTClaimsSet claims = processCognitoToken(idToken);
            validateIssuer(claims);
            verifyIfIdToken(claims);
            Credentials credentialsForIdentity = getCredentialsForIdentity(idToken);
            User user = new User(credentialsForIdentity.accessKeyId(),
                    credentialsForIdentity.secretKey(),
                    ImmutableList.of());
            return new JwtAuthentication(user, credentialsForIdentity.sessionToken(), null, null);
        }
        throw new InfoLedgerAuthenticationException("User name is null.");
    }

    public CognitoIdentityClient cognitoIdentityClient() {

        return CognitoIdentityClient.builder()
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();
    }

    private Credentials getCredentialsForIdentity(String token) throws InfoLedgerAuthenticationException {
        try {
            Map<String, String> providerTokens = ImmutableMap.of(jwtConfiguration.getIdentityProviderKey(), token);
            GetIdRequest request = GetIdRequest.builder()
                    .accountId(jwtConfiguration.getAwsAccountId())
                    .logins(providerTokens)
                    .identityPoolId(jwtConfiguration.getIdentityPoolId())
                    .build();

            CognitoIdentityClient cognitoIdentityClient = cognitoIdentityClient();
            GetIdResponse idResponse = cognitoIdentityClient.getId(request);
            String identityId = idResponse.identityId();

            GetCredentialsForIdentityRequest getCredentialsForIdentityRequest = GetCredentialsForIdentityRequest.builder()
                    .identityId(identityId)
                    .logins(providerTokens)
                    .build();

            GetCredentialsForIdentityResponse response = cognitoIdentityClient.getCredentialsForIdentity(getCredentialsForIdentityRequest);
            return response.credentials();
        } catch (CognitoIdentityProviderException e) {
            throw new InfoLedgerAuthenticationException("Can not get access token");
        }
    }

    private JWTClaimsSet processCognitoToken(String accessToken) throws InfoLedgerAuthenticationException {
        try {
            return configurableJWTProcessor.process(accessToken, null);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            log.error("Can not process token: {}", e.getMessage());
            throw new InfoLedgerAuthenticationException(e.getMessage());
        }
    }

    private void verifyIfIdToken(JWTClaimsSet claims) throws InfoLedgerAuthenticationException {
        if (!claims.getIssuer().equals(jwtConfiguration.getIdentityPoolUrl())) {
            throw new InfoLedgerAuthenticationException("JWT Token is not an ID Token");
        }
    }

    private void validateIssuer(JWTClaimsSet claims) throws InfoLedgerAuthenticationException {
        if (!claims.getIssuer().equals(jwtConfiguration.getIdentityPoolUrl())) {
            throw new InfoLedgerAuthenticationException(String.format("Issuer %s does not match cognito idp %s",
                    claims.getIssuer(),
                    jwtConfiguration.getIdentityPoolUrl()));
        }
    }

    private String getBearerToken(String token) {
        return token.startsWith("Bearer=") ? token.substring("Bearer=".length()) : token;
    }
}
