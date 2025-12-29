package org.tscd.controller;

import org.tscd.datalake.AmazonS3Provider;
import org.tscd.datalake.DatalakeBuilder;
import org.tscd.datalake.StorageProvider;
import org.tscd.messaging.QueuePublisher;
import org.tscd.messaging.SQSQueuePublisher;
import org.tscd.model.Movie;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        StorageProvider storageProvider = new AmazonS3Provider("bucket-tscd-prueba2");

        FilmProvider filmProvider = new FilmProvider("https://api.imdbapi.dev/titles?startYear=1950&endYear=1952");
        List<String> titleIds = filmProvider.getMovieId();
        List<Movie> movieList = filmProvider.getMovieList(titleIds);

        DatalakeBuilder datalakeBuilder = new DatalakeBuilder(storageProvider);

        String filepath = datalakeBuilder.write(movieList);
        datalakeBuilder.cloudStorage(filepath);

        QueuePublisher queuePublisher = new SQSQueuePublisher( "https://sqs.us-east-1.amazonaws.com/301998063112/movies-ingestion-queue");

        queuePublisher.publish("s3://bucket-tscd-prueba2/" + filepath);

        System.out.println(movieList);
    }
}
