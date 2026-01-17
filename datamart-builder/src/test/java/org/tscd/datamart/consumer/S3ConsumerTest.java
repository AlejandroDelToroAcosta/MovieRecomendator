package org.tscd.datamart.consumer;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.tscd.datamart.model.Movie;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3ConsumerTest {

    @Test
    void get_returnsMovieListFromJson() throws Exception {
        String json = "[{\"id\":\"tt1\",\"title\":\"Movie 1\",\"year\":2000}]";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());

        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        inputStream
                );

        S3Client mockClient = mock(S3Client.class);
        when(mockClient.getObject(any(GetObjectRequest.class)))
                .thenReturn(responseInputStream);

        S3Consumer consumer = new S3Consumer();

        Field clientField = S3Consumer.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(consumer, mockClient);

        List<Movie> movies = consumer.get("bucket", "key");

        assertNotNull(movies);
        assertEquals(1, movies.size());
        assertEquals("tt1", movies.get(0).getId());
    }
}