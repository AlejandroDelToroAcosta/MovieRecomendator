package org.tscd.api.lambda;

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
                .build();
    }

    // En tu proyecto de Spring Boot, clase LambdaQueryClient:
    public String call(String type, String value) {
        // 1. Creamos el JSON que el MovieQueryHandler espera recibir en su Map<String, String>
        String jsonPayload = String.format("{\"type\": \"%s\", \"value\": \"%s\"}", type, value);

        // 2. Preparamos la petición
        InvokeRequest request = InvokeRequest.builder()
                .functionName("MovieQueryHandler") // El nombre que pusimos en Terraform
                .payload(SdkBytes.fromUtf8String(jsonPayload))
                .build();

        try {
            // 3. Llamamos a la Lambda real
            InvokeResponse response = client.invoke(request);

            // 4. Obtenemos la respuesta
            return response.payload().asUtf8String();
        } catch (Exception e) {
            return "{\"error\": \"Fallo al llamar a la Lambda: " + e.getMessage() + "\"}";
        }
    }
}
