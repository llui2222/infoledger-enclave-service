package com.infoledger.crypto.encryption;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SystemDefaultDnsResolver;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EncryptionFactory {

  private EncryptionFactory() {
    // Empty ctor. due to creation of factory objects is not needed.
  }

  /**
   * Encryption factory. Credentials and local server address are always the same within the
   * instance (these are instance IAM role and enclave local address), so the only varying part is
   * the region thus storing encryptor per region. KMS Key Arn is specific per user.
   *
   * @param region Target region where the kms key resides
   * @param credentials AWS credentials with permission to encrypt/decrypt using key.
   * @param localServerAddress Enclave server address.
   * @param kmsKeyArn KMS Key id
   * @return Encryptor instance
   */
  public static EncryptionApi getEncryptor(
      String region, AWSCredentials credentials, InetAddress localServerAddress, String kmsKeyArn) {
    AWSKMS kmsClient =
        AWSKMSClientBuilder.standard()
            .withClientConfiguration(
                new ClientConfiguration()
                    .withDnsResolver(
                        new SystemDefaultDnsResolver() {
                          @Override
                          public InetAddress[] resolve(String host) throws UnknownHostException {
                            if (String.format("kms.%s.amazonaws.com", region).equals(host)) {
                              return new InetAddress[] {localServerAddress}; // for host redirection
                            } else {
                              return super.resolve(host);
                            }
                          }
                        }))
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    String.format("kms.%s.amazonaws.com:8443", region),
                    region // for port redirection
                    ))
            .withRequestHandlers(
                new RequestHandler2() {
                  @Override
                  public AmazonWebServiceRequest beforeExecution(AmazonWebServiceRequest request) {
                    return super.beforeExecution(request);
                  }
                })
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();
    final AwsCrypto crypto =
        AwsCrypto.builder()
            .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
            .build();

    final KmsMasterKeyProvider keyProvider =
        KmsMasterKeyProvider.builder()
            .withCustomClientFactory(new RegionalClientSupplierImpl(kmsClient))
            .withDefaultRegion(region)
            .buildStrict(kmsKeyArn);

    return new KMSEncryption(crypto, keyProvider);
  }
}
