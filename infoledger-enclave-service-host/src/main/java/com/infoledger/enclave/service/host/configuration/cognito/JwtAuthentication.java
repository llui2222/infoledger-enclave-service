package com.infoledger.enclave.service.host.configuration.cognito;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@EqualsAndHashCode
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final transient User principal;
    private final transient String sessionToken;
    private final JWTClaimsSet jwtClaimsSet;

    public JwtAuthentication(User principal, String sessionToken, JWTClaimsSet jwtClaimsSet, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.sessionToken = sessionToken;
        this.jwtClaimsSet = jwtClaimsSet;
        super.setAuthenticated(true);
    }

    @SneakyThrows
    @Override
    public Object getCredentials() {
        throw new IllegalAccessException("No credentials are used in InfoLedger service.");
    }

    public User getPrincipal() {
        return this.principal;
    }

    public JWTClaimsSet getJwtClaimsSet() {
        return this.jwtClaimsSet;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
