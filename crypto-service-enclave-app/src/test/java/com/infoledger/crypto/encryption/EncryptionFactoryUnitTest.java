package com.infoledger.crypto.encryption;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.amazonaws.auth.AWSCredentials;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;

class EncryptionFactoryUnitTest {
  @Test
  void testFactoryCanCreateEncryptionApi() {
    // When
    EncryptionApi encryptionApi =
        EncryptionFactory.getEncryptor(
            "us-east-1", mock(AWSCredentials.class), mock(InetAddress.class), "kmsKeyArn");

    // Then
    assertNotNull(encryptionApi);
  }
}
