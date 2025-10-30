package org.tscd.datalake;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.tscd.model.Movie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatalakeBuilder {
    private final String bucketName = "tscdff-bucket";
    private final String regionName = "us-east-1";
    private final AmazonS3 s3Client;

    public DatalakeBuilder() {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(regionName))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    public void write(List<Movie> movieList) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String localDirPath = "datalake/" + timestamp;
        String filePath = localDirPath + "/movies.csv";

        try {
            Files.createDirectories(new File(localDirPath).toPath());
            try (
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                            .withHeader("ID", "Title", "Year", "Cast", "Directors", "Genre", "Rating", "Duration"))
            ) {
                for (Movie movie : movieList) {
                    csvPrinter.printRecord(
                            movie.getId(),
                            movie.getTitle(),
                            movie.getYear(),
                            movie.getCast(),
                            movie.getDirectors(),
                            movie.getGenre(),
                            movie.getRating(),
                            movie.getDuration()
                    );
                }
                csvPrinter.flush();
                System.out.println("Archivo CSV guardado localmente en: " + filePath);
            }

            s3Client.putObject(new PutObjectRequest(bucketName, filePath, new File(filePath)));
            System.out.println("Archivo subido correctamente a S3: s3://" + bucketName + "/" + filePath);

        } catch (AmazonServiceException e) {
            System.err.println("Error de servicio S3: " + e.getErrorMessage());
        } catch (IOException e) {
            System.err.println("Error de E/S al escribir el CSV: " + e.getMessage());
        }
    }

    public void createBucket(String bucketName, String regionName) {
        Regions region = Regions.fromName(regionName);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        if (!s3Client.doesBucketExistV2(bucketName)) {
            try {
                s3Client.createBucket(bucketName);
                System.out.println("Bucket creado: " + bucketName + " en región " + regionName);
            } catch (AmazonServiceException e) {
                System.err.println("Error al crear el bucket: " + e.getErrorMessage());
            }
        } else {
            System.out.println("El bucket '" + bucketName + "' ya existe en la región " + regionName);
        }

    }

}
