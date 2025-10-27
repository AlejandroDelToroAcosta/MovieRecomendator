package org.tscd.controller;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        FilmProvider filmProvider = new FilmProvider("https://api.imdbapi.dev/titles");
        filmProvider.getMovieId();
    }
}
