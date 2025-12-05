package org.tscd.datamart.controller;

import com.google.gson.Gson;
import org.tscd.datamart.model.Movie;
import org.tscd.datamart.service.DatalakeConsumerService;
import org.tscd.datamart.service.S3Consumer;
import org.tscd.datamart.service.StorageConsumer;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        StorageConsumer storageConsumer = new S3Consumer("bucket-1234-15");
        DatalakeConsumerService datalakeConsumerService =new DatalakeConsumerService(storageConsumer) ;
        List<Movie> movies = datalakeConsumerService.consumerCall("movies.json");
        Gson gson = new Gson();

        System.out.println(gson.toJson(movies));
    }
}
