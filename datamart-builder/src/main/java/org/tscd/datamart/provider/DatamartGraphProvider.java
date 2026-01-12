package org.tscd.datamart.provider;

import org.tscd.datamart.model.Movie;

import java.util.List;

public class DatamartGraphProvider {
    private final InsertionService service;

    public DatamartGraphProvider(InsertionService service) {
        this.service = service;
    }

    public void writeCall(List<Movie> movies) {
        service.writeMovies(movies);
    }
}