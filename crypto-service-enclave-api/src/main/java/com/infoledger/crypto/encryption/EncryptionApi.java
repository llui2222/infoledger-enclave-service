package com.infoledger.crypto.encryption;

public interface EncryptionApi {

  /**
   * Decrypt the specified data with the specified key. The byte array data is supposed to be
   * base64-encoded.
   *
   * @param data Data to decrypt
   * @return Decrypted data
   */
  byte[] decrypt(byte[] data);

  /**
   * Encrypts the specified data with the specified key. Bsae64-encodes the result.
   *
   * @param data Data to encrypt
   * @return Base64-encoded encrypted data
   */
  byte[] encrypt(byte[] data);
}
