package org.tscd.datamart.consumer;

import org.tscd.datamart.model.Movie;

import java.util.List;

public class DatalakeConsumerService {
    private final StorageConsumer client;

    public DatalakeConsumerService(StorageConsumer client) {
        this.client = client;
    }
    public void consumerCall(String bucket, String key) throws Exception {
        //List<Movie> movieList = client.get(bucket, key);
        //return movieList;
    }
}