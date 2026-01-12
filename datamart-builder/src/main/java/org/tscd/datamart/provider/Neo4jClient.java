package org.tscd.datamart.provider;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Config;

import org.tscd.datamart.model.Movie;
import org.tscd.datamart.model.Person;



import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Neo4jClient implements AutoCloseable, InsertionService {

    private final Driver driver;

    public Neo4jClient(String uri, String user, String password) {
        Config config = Config.builder()
                .withoutEncryption()
                .build();
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password),config);
    }

    public void writeMovies(List<Movie> movies) {

        List<Map<String, Object>> movieMaps = movies.stream()
                .map(this::movieToMap)
                .toList();

        try (Session session = driver.session()) {

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("movies", movieMaps);

            session.writeTransaction(tx -> {
                tx.run(CREATE_MOVIE_GRAPH_QUERY, parameters);
                return null;
            });

            System.out.println("Carga de datos completada exitosamente. Películas cargadas: " + movies.size());

        } catch (Exception e) {
            System.err.println("Error durante la carga de datos en Neo4j: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Map<String, Object> movieToMap(Movie movie) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", movie.getId());
        map.put("title", movie.getTitle());
        map.put("year", movie.getYear());
        map.put("rating", movie.getRating());
        map.put("duration", movie.getDuration());
        map.put("genre", movie.getGenres());

        map.put("cast", movie.getCast().stream().map(this::personToMap).toList());
        map.put("directors", movie.getDirectors().stream().map(this::personToMap).toList());

        return map;
    }


    private Map<String, Object> personToMap(Person person) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", person.getId());
        map.put("name", person.getName());
        return map;
    }

    @Override
    public void close() {
        driver.close();
    }

    private static final String CREATE_MOVIE_GRAPH_QUERY = """
        UNWIND $movies AS movieData

        // Movie
        MERGE (m:Movie {id: movieData.id})
        ON CREATE SET 
            m.title = movieData.title, 
            m.year = movieData.year, 
            m.rating = movieData.rating,
            m.duration = movieData.duration
        ON MATCH SET
            m.title = movieData.title,
            m.year = movieData.year,
            m.rating = movieData.rating,
            m.duration = movieData.duration

        // Directors
        WITH m, movieData
        UNWIND (CASE WHEN movieData.directors IS NULL THEN [] ELSE movieData.directors END) AS directorData
        MERGE (d:Person:Director {id: directorData.id})
        ON CREATE SET d.name = directorData.name
        MERGE (d)-[:DIRECTED]->(m)

        // Cast
        WITH m, movieData
        UNWIND (CASE WHEN movieData.cast IS NULL THEN [] ELSE movieData.cast END) AS actorData
        MERGE (a:Person:Actor {id: actorData.id})
        ON CREATE SET a.name = actorData.name
        MERGE (a)-[:ACTED_IN]->(m)

        // Genre
        WITH m, movieData
        UNWIND (CASE WHEN movieData.genre IS NULL THEN [] ELSE movieData.genre END) AS genreName
        MERGE (g:Genre {name: genreName})
        MERGE (m)-[:HAS_GENRE]->(g)
        """;
}