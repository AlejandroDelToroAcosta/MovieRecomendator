package org.tscd.api.lambda;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.SdkBytes;
import org.springframework.stereotype.Component;import org.springframework.stereotype.Component;

@Component
public class LambdaQueryClient {

    private final LambdaClient client;

    public LambdaQueryClient() {
        this.client = LambdaClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider())
                .build();
    }
    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    // En tu proyecto de Spring Boot, clase LambdaQueryClient:
    public String call(String type, String value) {
        String jsonPayload = String.format("{\"type\": \"%s\", \"value\": \"%s\"}", type, value);

        InvokeRequest request = InvokeRequest.builder()
                .functionName("movies-query-api")
                .payload(SdkBytes.fromUtf8String(jsonPayload))
                .build();

        try {
            InvokeResponse response = client.invoke(request);
            return response.payload().asUtf8String();
        } catch (Exception e) {
            return "{\"error\": \"Fallo al conectar con AWS: " + e.getMessage() + "\"}";
        }
    }
}
