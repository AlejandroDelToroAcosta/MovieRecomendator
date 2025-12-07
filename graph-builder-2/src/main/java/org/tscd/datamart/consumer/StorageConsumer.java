package org.tscd.datamart.consumer;

import org.tscd.datamart.model.Movie;

import java.util.List;

public interface StorageConsumer {
    List<Movie> get(String key) throws Exception;
}
