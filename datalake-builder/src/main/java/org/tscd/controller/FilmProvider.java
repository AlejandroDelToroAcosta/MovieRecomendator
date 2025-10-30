package org.tscd.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.tscd.model.Actor;
import org.tscd.model.Director;
import org.tscd.model.Movie;
import org.tscd.utils.HTTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.tscd.utils.JsonUtils.*;

public class FilmProvider {

    private final String url;

    public FilmProvider(String url) {
        this.url = url;
    }

    public List<String> getMovieId() throws IOException {
        List<String> titleList = new ArrayList<>();
        String json = HTTPClient.get(url);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        JsonArray titles = jsonObject.get("titles").getAsJsonArray();

        for (int i = 0; i < titles.size(); i++) {
            JsonObject movie = titles.get(i).getAsJsonObject();
            String id = movie.get("id").getAsString();

            titleList.add(id);
        }

        System.out.println(titleList);
        return titleList;
    }
    public List<Movie> getMovieList(List<String> movieIds) throws IOException {
        List<Movie> movieList = new ArrayList<>();


            String newUrl = "https://api.imdbapi.dev/titles/" + "tt0054215";
            String json = HTTPClient.get(newUrl);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            String title = jsonObject.get("primaryTitle").getAsString();
            int year = jsonObject.get("startYear").getAsInt();

            JsonArray genresJsonArray = jsonObject.get("genres").getAsJsonArray();
            List<String> genres = new ArrayList<>();
            for (int i = 0; i < genresJsonArray.size(); i++) {
                String genre = genresJsonArray.get(i).getAsString();
                genres.add(genre);
            }

            double rating = getSafeDouble(jsonObject.getAsJsonObject("rating"), "aggregateRating");

            List<Director> directorsList = new ArrayList<>();
            JsonArray directors = jsonObject.get("directors").getAsJsonArray();
            for (int j = 0; j < directors.size(); j++) {
                JsonObject director = directors.get(j).getAsJsonObject();
                String id = director.get("id").getAsString();
                String name = director.get("displayName").getAsString();
                Director newDirector = new Director(id, name);
                directorsList.add(newDirector);
            }

            List<Actor> actorsList = new ArrayList<>();
            JsonArray stars =getSafeArray(jsonObject, "stars");
            for (int j = 0; j < stars.size(); j++) {
                JsonObject actor = stars.get(j).getAsJsonObject();
                String id = actor.get("id").getAsString();
                String name = actor.get("displayName").getAsString();
                Actor newActor = new Actor(id, name);
                actorsList.add(newActor);
            }
            int seconds = getSafeInt(jsonObject, "runtimeSeconds");


            Movie movie = new Movie("tt0054215", title, year, actorsList, directorsList, genres, rating, seconds);
            movieList.add(movie);

        return movieList;
    }
}
