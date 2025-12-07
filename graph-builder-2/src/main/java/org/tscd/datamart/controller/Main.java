package org.tscd.datamart.controller;

import com.google.gson.Gson;
import org.tscd.datamart.model.Movie;
import org.tscd.datamart.consumer.DatalakeConsumerService;
import org.tscd.datamart.provider.InsertionService;
import org.tscd.datamart.provider.Neo4jClient;
import org.tscd.datamart.consumer.S3Consumer;
import org.tscd.datamart.consumer.StorageConsumer;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        StorageConsumer storageConsumer = new S3Consumer("bucket-1234-15");
        DatalakeConsumerService datalakeConsumerService =new DatalakeConsumerService(storageConsumer) ;
        List<Movie> movies = datalakeConsumerService.consumerCall("movies.json");

        InsertionService service = new Neo4jClient(args[0], args[1], args[2]);
        service.writeMovies(movies);
    }
}
