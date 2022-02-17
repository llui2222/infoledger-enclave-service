package com.infoledger.enclave.service.host.configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud")
public class S3ClientConfiguration {

    @Bean
    public AmazonS3 getS3Client() {
        // Get AmazonS3 client and return the s3Client object.
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}
