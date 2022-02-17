package com.infoledger.crypto.encryption;

import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kms.AWSKMS;

/** {@link KmsMasterKeyProvider.RegionalClientSupplier} default implementation */
public class RegionalClientSupplierImpl implements KmsMasterKeyProvider.RegionalClientSupplier {

  private final AWSKMS kmsClient;

  RegionalClientSupplierImpl(AWSKMS kmsClient) {
    this.kmsClient = kmsClient;
  }

  @Override
  public AWSKMS getClient(String regionName) {
    return kmsClient;
  }
}
