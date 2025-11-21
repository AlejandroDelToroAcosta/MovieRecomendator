package org.tscd.controller;

import org.tscd.datalake.AmazonS3Provider;
import org.tscd.datalake.DatalakeBuilder;
import org.tscd.datalake.StorageProvider;
import org.tscd.model.Movie;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // TODO
        // Esconder rutas


        StorageProvider storageProvider = new AmazonS3Provider("bucket-1234");

        FilmProvider filmProvider = new FilmProvider("https://api.imdbapi.dev/titles?startYear=1950&endYear=1960");
        List<String> titleIds = filmProvider.getMovieId();
        List<Movie> movieList = filmProvider.getMovieList(titleIds);

        DatalakeBuilder datalakeBuilder = new DatalakeBuilder(storageProvider);

        datalakeBuilder.cloudStorage(
                datalakeBuilder.write(movieList));

        System.out.println(movieList);
    }
}
