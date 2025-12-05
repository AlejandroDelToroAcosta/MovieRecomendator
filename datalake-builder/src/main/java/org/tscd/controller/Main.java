package org.tscd.controller;

import org.tscd.datalake.AmazonS3Provider;
import org.tscd.datalake.DatalakeBuilder;
import org.tscd.datalake.StorageProvider;
import org.tscd.model.Movie;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {



        StorageProvider storageProvider = new AmazonS3Provider("bucket-1234-15");

        FilmProvider filmProvider = new FilmProvider("https://api.imdbapi.dev/titles?startYear=1950&endYear=1960");
        List<String> titleIds = filmProvider.getMovieId();
        List<Movie> movieList = filmProvider.getMovieList(titleIds);

        DatalakeBuilder datalakeBuilder = new DatalakeBuilder(storageProvider);

        datalakeBuilder.cloudStorage("C:\\Users\\aadel\\Desktop\\GCID\\Cuarto\\Primer Cuatrimestre\\TSCD\\Trabajo\\film-recomendator\\datalake\\2025-11-29_12-24-39\\movies.json");
        datalakeBuilder.write(movieList);

        System.out.println(movieList);
    }
}
