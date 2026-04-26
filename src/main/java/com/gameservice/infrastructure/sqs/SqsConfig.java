package com.gameservice.infrastructure.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SqsConfig {

    @Value("${aws.sqs.endpoint}")
    private String awsEndpoint;

    @Value("${aws.sqs.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key-id:test}")
    private String accessKey;

    @Value("${aws.secret-access-key:test}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        var b = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            b.endpointOverride(URI.create(awsEndpoint));
        }
        return b.build();
    }

    @Bean(destroyMethod = "close")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
