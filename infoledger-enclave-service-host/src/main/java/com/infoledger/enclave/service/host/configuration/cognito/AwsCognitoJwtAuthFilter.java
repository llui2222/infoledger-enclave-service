package com.infoledger.enclave.service.host.configuration.cognito;

import com.infoledger.enclave.service.host.exception.InfoLedgerAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class AwsCognitoJwtAuthFilter extends OncePerRequestFilter {
    private final AwsCognitoIdTokenProcessor cognitoIdTokenProcessor;

    public AwsCognitoJwtAuthFilter(AwsCognitoIdTokenProcessor cognitoIdTokenProcessor) {
        this.cognitoIdTokenProcessor = cognitoIdTokenProcessor;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws IOException {
        try {
            Authentication authentication = cognitoIdTokenProcessor.authenticate(request);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (InfoLedgerAuthenticationException ex) {
            log.error("Cognito ID Token processing error, clearing security context: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException ex) {
            log.error("Privileged computation failed.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/swagger")
                || path.startsWith("/configuration")
                || path.startsWith("/webjars")
                || path.equals("/v2/api-docs")
                || path.equals("/actuator/health");
    }
}

