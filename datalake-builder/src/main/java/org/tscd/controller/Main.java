package org.tscd.controller;

import org.tscd.datalake.DatalakeBuilder;
import org.tscd.model.Movie;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        FilmProvider filmProvider = new FilmProvider("https://api.imdbapi.dev/titles?startYear=1950&endYear=1960");
        List<String> titleIds = filmProvider.getMovieId();
        List<Movie> movieList = filmProvider.getMovieList(titleIds);
        DatalakeBuilder datalakeBuilder = new DatalakeBuilder();
        datalakeBuilder.write(movieList);
        //datalakeBuilder.createBucket("tscdff2-bucket", "us-east-1");

        System.out.println(movieList);
    }
}
