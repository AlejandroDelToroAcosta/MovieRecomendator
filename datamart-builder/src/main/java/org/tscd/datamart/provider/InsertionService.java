package org.tscd.datamart.provider;

import org.tscd.datamart.model.Movie;

import java.util.List;

public interface InsertionService {
    void writeMovies(List<Movie> movies);
}