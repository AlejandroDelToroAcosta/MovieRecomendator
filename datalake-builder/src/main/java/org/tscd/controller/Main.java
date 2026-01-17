package org.tscd.controller;

import io.github.cdimascio.dotenv.Dotenv;
import org.tscd.datalake.AmazonS3Provider;
import org.tscd.datalake.DatalakeBuilder;
import org.tscd.datalake.StorageProvider;
import org.tscd.messaging.QueuePublisher;
import org.tscd.messaging.SQSQueuePublisher;
import org.tscd.model.Movie;

import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {


    public static void main(String[] args) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String bucketName = dotenv.get("BUCKET_NAME");
        String sqsQueueUrl = dotenv.get("SQS_QUEUE_URL");

        StorageProvider storageProvider =
                new AmazonS3Provider(bucketName);

        int endYear = 1955;

        for (int i = 0; i< 20; i+=5) {
            FilmProvider filmProvider =
                    new FilmProvider("https://api.imdbapi.dev/titles?startYear=1953&endYear=" + endYear);

            List<String> titleIds = filmProvider.getMovieId();

            List<Movie> movieList = filmProvider.getMovieList(titleIds);

            DatalakeBuilder datalakeBuilder =
                    new DatalakeBuilder(
                            storageProvider);

            String filepath = datalakeBuilder.write(movieList);

            datalakeBuilder.cloudStorage(filepath);

            QueuePublisher queuePublisher =
                    new SQSQueuePublisher(
                            sqsQueueUrl);

            queuePublisher.publish("s3://" + bucketName + "/" + filepath);

            System.out.println(movieList);
            endYear = endYear+5;


            try {
                sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
