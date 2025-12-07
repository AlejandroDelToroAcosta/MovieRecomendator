package org.tscd.datamart.provider;

import org.tscd.datamart.model.Movie;

import java.util.List;

public class DatamartGraphpProvider {
    private final InsertionService service;

    public DatamartGraphpProvider(InsertionService service) {
        this.service = service;
    }

    public void writeCall(List<Movie> movies) {
        service.writeMovies(movies);
    }
}