package org.tscd.datamart.consumer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.tscd.datamart.model.Movie;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import static software.amazon.awssdk.regions.Region.US_EAST_1;


public class S3Consumer implements StorageConsumer {
    private final S3Client client;
    private final Gson gson;

    public S3Consumer() {
        this.gson = new Gson();
        this.client = S3Client.builder()
                .region(US_EAST_1)
                .credentialsProvider(credentialsProvider())
                .build();
    }

    public List<Movie> get(String bucket, String key) throws Exception {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            InputStream s3Stream = client.getObject(getObjectRequest);

            try (Reader reader = new InputStreamReader(s3Stream)) {

                Type movieListType = new TypeToken<List<Movie>>() {}.getType();
                List<Movie> movies = gson.fromJson(reader, movieListType);

                s3Stream.close();
                return movies;
            }
    }
    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

}
