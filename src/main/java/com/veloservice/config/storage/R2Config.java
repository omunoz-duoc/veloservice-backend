package com.veloservice.config.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    static {
        // AWS SDK v2 >= 2.30 adds a CRC32 checksum to PutObject by default (WHEN_SUPPORTED),
        // baking an `x-amz-sdk-checksum-algorithm` header into presigned PUT URLs. A plain
        // binary PUT (e.g. from the mobile client) can't reproduce that header, so R2 rejects
        // it with 403 SignatureDoesNotMatch. The S3Presigner builder has no checksum knob, so
        // we relax the default to WHEN_REQUIRED via the system property the SDK resolves from.
        System.setProperty("aws.requestChecksumCalculation", "when_required");
    }

    @Bean
    public S3Client r2Client(R2Properties properties) {
        return S3Client.builder()
                .endpointOverride(endpoint(properties))
                .region(Region.of("auto"))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .build();
    }

    @Bean
    public S3Presigner r2Presigner(R2Properties properties) {
        return S3Presigner.builder()
                .endpointOverride(endpoint(properties))
                .region(Region.of("auto"))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private URI endpoint(R2Properties properties) {
        return URI.create("https://" + properties.accountId() + ".r2.cloudflarestorage.com");
    }

    private StaticCredentialsProvider credentials(R2Properties properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())
        );
    }
}
