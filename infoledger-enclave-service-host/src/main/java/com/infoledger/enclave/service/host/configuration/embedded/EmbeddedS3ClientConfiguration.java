package com.infoledger.enclave.service.host.configuration.embedded;

import akka.http.scaladsl.Http;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@Profile(value = {"local | test"})
@Slf4j
public class EmbeddedS3ClientConfiguration {
    @Value("${amazon.aws.bucket.attachments:}")
    private String awsBucketForAttachments;

    @Value("${amazon.aws.bucket.aggregations:}")
    private String awsBucketAggregationResults;

    @Value("${amazon.aws.s3.endpoint:}")
    private String awsS3EndPoint;

    @Value("${amazon.aws.s3.port:}")
    private Integer awsS3EndPort;

    private S3Mock api;

    /**
     * Starting embedded S3 for application
     */
    @PostConstruct
    public void s3MockApi() {
        api = new S3Mock.Builder()
                .withPort(awsS3EndPort)
                .withInMemoryBackend()
                .build();

        try {
            Http.ServerBinding binding = api.start();
            int port = binding.localAddress().getPort();
            log.info("S3 mocked service started on port: {}", port);
        } catch (Exception ex) {
            log.info("S3 mocked service already started");
        }
    }

    @Bean
    @Primary
    public AmazonS3 getS3Client() {
        // Get AmazonS3 client and return the s3Client object.
        // Get AmazonS3 client and return the s3Client object.
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder
                        .EndpointConfiguration(awsS3EndPoint, Regions.US_EAST_1.getName()))
                .build();
        amazonS3.createBucket(awsBucketForAttachments);
        amazonS3.createBucket(awsBucketAggregationResults);
        return amazonS3;
    }

    @PreDestroy
    public void teadDownS3() {
        api.shutdown();
        log.info("S3 mocked service destroyed");
    }
}
