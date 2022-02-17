package com.infoledger.enclave.service.host.service.impl;

import com.infoledger.enclave.service.host.configuration.cognito.JwtAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {

  private TestUtil() {
    // empty ctor.
  }

  static void testAuth() {
    User user = mock(User.class);
    when(user.getUsername()).thenReturn("accessKey");
    when(user.getPassword()).thenReturn("secretKey");

    JwtAuthentication auth = mock(JwtAuthentication.class);
    when(auth.getPrincipal()).thenReturn(user);
    when(auth.getSessionToken()).thenReturn("sessionToken");

    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
