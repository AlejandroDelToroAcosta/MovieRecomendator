package org.tscd.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.tscd.model.Actor;
import org.tscd.model.Director;
import org.tscd.model.Movie;
import org.tscd.utils.HTTPClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilmProviderTest {

    private FilmProvider filmProvider;

    @BeforeEach
    void setUp() {
        filmProvider = new FilmProvider("http://fakeurl.com");
    }

    @Test
    void testGetMovieId_returnsListOfIds() throws IOException {
        String jsonResponse = """
            {
              "titles": [
                {"id": "tt0001"},
                {"id": "tt0002"}
              ]
            }
            """;

        try (MockedStatic<HTTPClient> mockedHttp = mockStatic(HTTPClient.class)) {
            mockedHttp.when(() -> HTTPClient.get("http://fakeurl.com")).thenReturn(jsonResponse);

            List<String> ids = filmProvider.getMovieId();
            assertEquals(2, ids.size());
            assertTrue(ids.contains("tt0001"));
            assertTrue(ids.contains("tt0002"));
        }
    }

    @Test
    void testGetMovieList_returnsListOfMovies() throws IOException {
        List<String> ids = List.of("tt0001");

        String movieJson = """
            {
              "primaryTitle": "Test Movie",
              "startYear": 2020,
              "genres": ["Action", "Drama"],
              "rating": {"aggregateRating": 7.5},
              "directors": [{"id": "d1", "displayName": "Director One"}],
              "stars": [{"id": "a1", "displayName": "Actor One"}],
              "runtimeSeconds": 7200
            }
            """;

        try (MockedStatic<HTTPClient> mockedHttp = mockStatic(HTTPClient.class)) {
            mockedHttp.when(() -> HTTPClient.get("https://api.imdbapi.dev/titles/tt0001")).thenReturn(movieJson);

            List<Movie> movies = filmProvider.getMovieList(ids);
            assertEquals(1, movies.size());

            Movie movie = movies.get(0);
            assertEquals("tt0001", movie.getId());
            assertEquals("Test Movie", movie.getTitle());
            assertEquals(2020, movie.getYear());
            assertEquals(7.5, movie.getRating());
            assertEquals(2, movie.getGenre().size());
            assertEquals(1, movie.getCast().size());
            assertEquals(1, movie.getDirectors().size());

            Actor actor = movie.getCast().get(0);
            assertEquals("a1", actor.getId());
            assertEquals("Actor One", actor.getName());

            Director director = movie.getDirectors().get(0);
            assertEquals("d1", director.getId());
            assertEquals("Director One", director.getName());
        }
    }

    @Test
    void testGetMovieList_handlesEmptyStarsArray() throws IOException {
        List<String> ids = List.of("tt0002");

        String movieJson = """
            {
              "primaryTitle": "Movie Without Stars",
              "startYear": 2021,
              "genres": ["Comedy"],
              "rating": {"aggregateRating": 6.0},
              "directors": [{"id": "d2", "displayName": "Director Two"}],
              "runtimeSeconds": 5400
            }
            """;

        try (MockedStatic<HTTPClient> mockedHttp = mockStatic(HTTPClient.class)) {
            mockedHttp.when(() -> HTTPClient.get("https://api.imdbapi.dev/titles/tt0002")).thenReturn(movieJson);

            List<Movie> movies = filmProvider.getMovieList(ids);
            assertEquals(1, movies.size());
            assertTrue(movies.get(0).getCast().isEmpty());
        }
    }
}
