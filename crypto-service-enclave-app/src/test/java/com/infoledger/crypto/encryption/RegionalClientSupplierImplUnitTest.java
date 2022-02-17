package com.infoledger.crypto.encryption;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.kms.AWSKMSClient;
import org.junit.jupiter.api.Test;

class RegionalClientSupplierImplUnitTest {
  @Test
  void testReturnSameKmsClientWithWhichWasCreated() {
    // Given
    AWSKMSClient kmsClient = mock(AWSKMSClient.class);

    // When
    RegionalClientSupplierImpl regionalClientSupplier = new RegionalClientSupplierImpl(kmsClient);

    // Then
    assertSame(kmsClient, regionalClientSupplier.getClient("anyRegion"));
  }
}
