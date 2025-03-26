package vttp.server.configs;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${do.space.key}")
    private String accessKey;

    @Value("${do.space.secret}")
    private String secretKey;

    @Value("${do.space.endpoint}")
    private String endPoint;

    @Value("${do.space.region}")
    private String endPointRegion;

    @Bean
    public S3Client getS3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
            .region(Region.of(endPointRegion))
            .endpointOverride(URI.create(endPoint))
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();
    }


    
}
