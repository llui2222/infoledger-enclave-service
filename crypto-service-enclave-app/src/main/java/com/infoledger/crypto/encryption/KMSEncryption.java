package com.infoledger.crypto.encryption;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import java.util.Base64;

/** Encryption client based on encryption-sdk library. */
public class KMSEncryption implements EncryptionApi {

  private final AwsCrypto crypto;
  private final KmsMasterKeyProvider keyProvider;

  public KMSEncryption(AwsCrypto crypto, KmsMasterKeyProvider keyProvider) {
    this.crypto = crypto;
    this.keyProvider = keyProvider;
  }

  @Override
  public byte[] decrypt(byte[] data) {
    byte[] decodedBytes = Base64.getMimeDecoder().decode(data);
    CryptoResult<byte[], KmsMasterKey> cryptoResult = crypto.decryptData(keyProvider, decodedBytes);

    return cryptoResult.getResult();
  }

  @Override
  public byte[] encrypt(byte[] data) {
    CryptoResult<byte[], KmsMasterKey> cryptoResult = crypto.encryptData(keyProvider, data);

    return Base64.getMimeEncoder().encode(cryptoResult.getResult());
  }
}
