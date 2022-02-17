package com.infoledger.enclave.service.host.configuration.cognito;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "cognito.jwk")
public class JwtConfiguration {
    private String userPoolId;
    private String identityPoolId;
    private String identityPoolUrl;
    private String jwkUrl;
    private String region;
    private String userNameField;
    private int connectionTimeout;
    private int readTimeout;
    private String httpHeader;
    private String cognitoClientId;
    private String identityProviderKey;
    private String awsAccountId;

    public String getJwkUrl() {
        return jwkUrl;
    }

    public void setJwkUrl(String jwkUrl) {
        this.jwkUrl = String.format(jwkUrl, region, userPoolId);
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public String getIdentityPoolId() {
        return identityPoolId;
    }

    public void setIdentityPoolId(String identityPoolId) {
        this.identityPoolId = identityPoolId;
    }

    public String getIdentityPoolUrl() {
        return identityPoolUrl;
    }

    public void setIdentityPoolUrl(String identityPoolUrl) {
        this.identityPoolUrl = String.format(identityPoolUrl, region, userPoolId);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUserNameField() {
        return userNameField;
    }

    public void setUserNameField(String userNameField) {
        this.userNameField = userNameField;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getHttpHeader() {
        return httpHeader;
    }

    public void setHttpHeader(String httpHeader) {
        this.httpHeader = httpHeader;
    }

    public String getCognitoClientId() {
        return cognitoClientId;
    }

    public void setCognitoClientId(String cognitoClientId) {
        this.cognitoClientId = cognitoClientId;
    }

    public String getIdentityProviderKey() {
        return identityProviderKey;
    }

    public void setIdentityProviderKey(String identityProviderKey) {
        this.identityProviderKey = identityProviderKey;
    }

    public String getAwsAccountId() {
        return awsAccountId;
    }

    public void setAwsAccountId(String awsAccountId) {
        this.awsAccountId = awsAccountId;
    }
}
