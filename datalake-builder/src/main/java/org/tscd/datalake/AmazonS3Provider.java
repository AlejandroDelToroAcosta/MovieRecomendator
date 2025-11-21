package org.tscd.datalake;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.File;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

public class AmazonS3Provider implements StorageProvider {
    private final S3Client client;
    private final String bucket;

    public AmazonS3Provider(String bucket) {
        this.bucket = bucket;
        this.client = S3Client.builder()
                .region(US_EAST_1)
                .credentialsProvider(credentialsProvider())
                .build();
    }

    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    public boolean bucketExists(String bucketName) {
        try {
            client.headBucket(b -> b.bucket(bucketName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public void createStorage() {
        if (!bucketExists(bucket)) {
            try {
                client.createBucket(b -> b.bucket(bucket));
                System.out.println("Bucket creado: " + bucket);
            } catch (Exception e) {
                System.err.println("Error al crear el bucket: " + e.getMessage());
            }
        } else {
            System.out.println("El bucket '" + bucket + "' ya existe.");
        }
    }
    @Override
    public void save(String filePath) {
        File file = new File(filePath);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(new File(filePath).getName())
                .build();

        client.putObject(request, RequestBody.fromFile(file));
        System.out.println("Archivo subido correctamente a S3: s3://" + bucket + "/" + filePath);
    }


}
