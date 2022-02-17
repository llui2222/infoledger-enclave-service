package com.infoledger.crypto.encryption;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KMSEncryptionTest {

  private static final String FILE_1_TXT = "file1.txt";
  private static final String FILE_2_TXT = "file2.txt";
  private static final String FILE_3_TXT = "file3.txt";

  private static final String FILE_1_TXT_REENCRYPTED = "1.txt";
  private static final String FILE_2_TXT_REENCRYPTED = "2.txt";
  private static final String FILE_3_TXT_REENCRYPTED = "3.txt";

  @Mock
  private CryptoResult<byte[], KmsMasterKey> cryptoResult;

  private final AwsCrypto awsCrypto = mock(AwsCrypto.class);
  private final KmsMasterKeyProvider kmsMasterKeyProvider = mock(KmsMasterKeyProvider.class);

  private final KMSEncryption kmsEncryption = new KMSEncryption(awsCrypto, kmsMasterKeyProvider);

  @Test
  void testEncryptFile() throws IOException, URISyntaxException {
    // Given
    byte[] data = loadResource(FILE_1_TXT);
    when(cryptoResult.getResult()).thenReturn(data);
    when(awsCrypto.encryptData(kmsMasterKeyProvider, data)).thenReturn(cryptoResult);

    // When
    byte[] result = kmsEncryption.encrypt(data);

    // Then
    verify(awsCrypto).encryptData(kmsMasterKeyProvider, data);
    assertEquals(
            new String(data, StandardCharsets.UTF_8),
            new String(Base64.getMimeDecoder().decode(result), StandardCharsets.UTF_8));
  }

  @Test
  void testDecryptFile() throws IOException, URISyntaxException {
    // Given
    byte[] data = loadResource(FILE_2_TXT);
    byte[] encodedData = Base64.getMimeEncoder().encode(data);
    when(cryptoResult.getResult()).thenReturn(data);
    when(awsCrypto.decryptData(kmsMasterKeyProvider, data)).thenReturn(cryptoResult);

    // When
    byte[] result = kmsEncryption.decrypt(encodedData);

    // Then
    verify(awsCrypto).decryptData(kmsMasterKeyProvider, data);
    assertEquals(
            new String(data, StandardCharsets.UTF_8), new String(result, StandardCharsets.UTF_8));
  }

  @Disabled("Needed for only preparing encrypted files for demo")
  @Test
  void testForEncryptingFilesForDemo() throws IOException, URISyntaxException {
    /*
     * Pre-req: You should have AWS cli installed on your machine and your aws user
     * should have a role with access to valid KMS key and
     * permissions for encrypt/decrypt/generateDataKey.
     *
     * Usage: copy KMS Key arn for KMS key described in pre-req and initialize
     * variable kmsKeyArn with it.
     */
    String kmsKeyArn = "arn:aws:kms:us-east-1:840966547573:key/af001e77-2733-4c23-9477-5ca8beee5be5";
    KMSEncryption demoKmsEncryption = getKmsEncryption(kmsKeyArn);

    /*
     * Pre-req: Prepare/create files you want to encrypt and put all of them to
     * crypto-service-enclave-app/src/test/resources folder
     *
     * Usage: Copy all file names for files described above and put into fileNames list.
     */
    List<String> fileNames = Arrays.asList(FILE_1_TXT, FILE_2_TXT, FILE_3_TXT);
    fileNames = Collections.singletonList("longtext.txt");

    /*
     * After following loop execution you can find encrypted files in
     * crypto-service-enclave-app/target/test-classes
     *
     * ** They will have prefix Encrypted
     */
    for (String fileName : fileNames) {
      byte[] data = loadResource(fileName);
      byte[] encryptedEncodedData = demoKmsEncryption.encrypt(data);

      assertNotNull(encryptedEncodedData);

      storeResource(fileName, "Encrypted", encryptedEncodedData);
    }
  }

  @Disabled("Needed for only decrypt files for demo")
  @Test
  void testForDecryptingFilesForDemo() throws IOException, URISyntaxException {
    /*
     * Pre-req: You should have AWS cli installed on your machine and your aws user
     * should have a role with access to valid KMS key and
     * permissions for encrypt/decrypt/generateDataKey.
     *
     * Usage: copy KMS Key arn for KMS key described in pre-req and initialize
     * variable kmsKeyArn with it.
     */
    String kmsKeyArn = "arn:aws:kms:us-east-1:840966547573:key/af001e77-2733-4c23-9477-5ca8beee5be5";
    KMSEncryption demoKmsEncryption = getKmsEncryption(kmsKeyArn);

    /*
     * Pre-req: Prepare files you want to decrypt and put all of them to
     * crypto-service-enclave-app/src/test/resources folder
     *
     * Usage: Copy all file names for files described above and put into fileNames list.
     */
    List<String> fileNames = Arrays.asList(FILE_1_TXT_REENCRYPTED, FILE_2_TXT_REENCRYPTED, FILE_3_TXT_REENCRYPTED);
    fileNames = Collections.singletonList("after_reencryption_cat.jpg");

    /*
     * After following loop execution you can find decrypted files in
     * crypto-service-enclave-app/target/test-classes
     *
     * ** They will have prefix Decrypted
     */
    for (String fileName : fileNames) {
      byte[] data = loadResource(fileName);
      byte[] decryptedDecodedData = demoKmsEncryption.decrypt(data);

      assertNotNull(decryptedDecodedData);

      storeResource(fileName, "Decrypted", decryptedDecodedData);
    }
  }

  private KMSEncryption getKmsEncryption(String kmsKeyArn) {
    AwsCrypto crypto =
            AwsCrypto.builder()
                    .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
                    .build();
    KmsMasterKeyProvider keyProvider =
            KmsMasterKeyProvider.builder()
                    .buildStrict(kmsKeyArn);

    return new KMSEncryption(crypto, keyProvider);
  }

  private byte[] loadResource(String fileName) throws IOException, URISyntaxException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);
    Path path = Paths.get(Objects.requireNonNull(resource).toURI());

    return Files.readAllBytes(path);
  }

  private void storeResource(String fileName, String prefix, byte[] data) throws IOException, URISyntaxException {
    ClassLoader classLoader = getClass().getClassLoader();
    File resourcesDirectory =
            Paths.get(Objects.requireNonNull(classLoader.getResource(fileName)).toURI())
                    .toFile()
                    .getParentFile();

    InputStream inputStream = new ByteArrayInputStream(data);
    File resultFile = new File(resourcesDirectory, prefix + fileName);
    FileUtils.copyInputStreamToFile(inputStream, resultFile);
  }
}
