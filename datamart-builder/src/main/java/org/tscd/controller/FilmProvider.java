package org.tscd.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.tscd.utils.HTTPClient;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

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
}
