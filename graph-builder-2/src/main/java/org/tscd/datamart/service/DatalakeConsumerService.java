package org.tscd.datamart.service;

import org.tscd.datamart.model.Movie;

import java.util.List;

public class DatalakeConsumerService {
    private final StorageConsumer client;

    public DatalakeConsumerService(StorageConsumer client) {
        this.client = client;
    }
    public List<Movie> consumerCall(String key) throws Exception {
        List<Movie> movieList = client.get(key);
        return movieList;
    }
}
