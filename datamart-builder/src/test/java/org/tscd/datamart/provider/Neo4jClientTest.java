package org.tscd.datamart.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.tscd.datamart.model.Actor;
import org.tscd.datamart.model.Director;
import org.tscd.datamart.model.Movie;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class Neo4jClientTest {

    private Driver mockDriver;
    private Session mockSession;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockDriver = mock(Driver.class);
        mockSession = mock(Session.class);
        mockTransaction = mock(Transaction.class);

        when(mockDriver.session()).thenReturn(mockSession);

        when(mockSession.writeTransaction(any())).thenAnswer(invocation -> {
            TransactionWork<?> work = invocation.getArgument(0);
            return work.execute(mockTransaction);
        });
    }

    @Test
    void writeMovies_executesQuery_withFullMovieData() throws Exception {
        Neo4jClient client = new Neo4jClient("bolt://localhost:7687", "user", "pass");

        Field driverField = Neo4jClient.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(client, mockDriver);

        Actor actor = new Actor("a1", "Actor 1");
        Director director = new Director("d1", "Director 1");

        Movie movie = new Movie();
        movie.setId("tt1");
        movie.setTitle("Movie 1");
        movie.setYear(2020);
        movie.setRating(8.5);
        movie.setDuration(120);
        movie.setGenres(List.of("Action", "Thriller"));
        movie.setCast(List.of(actor));
        movie.setDirectors(List.of(director));

        client.writeMovies(List.of(movie));

        verify(mockSession, times(1)).writeTransaction(any());

        verify(mockTransaction, times(1)).run(anyString(), any(Map.class));
    }

    @Test
    void close_callsDriverClose() throws Exception {
        Neo4jClient client = new Neo4jClient("bolt://localhost:7687", "user", "pass");

        Field driverField = Neo4jClient.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(client, mockDriver);

        client.close();

        verify(mockDriver, times(1)).close();
    }

    @Test
    void movieToMap_andPersonToMap_workCorrectly() throws Exception {
        Neo4jClient client = new Neo4jClient("bolt://localhost:7687", "user", "pass");

        Field driverField = Neo4jClient.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(client, mockDriver);

        Movie movie = new Movie();
        movie.setId("m1");
        movie.setTitle("Movie Test");
        movie.setYear(2022);
        movie.setRating(7.2);
        movie.setDuration(100);
        movie.setGenres(List.of("Comedy"));
        movie.setCast(List.of(new Actor("a1", "Actor Test")));
        movie.setDirectors(List.of(new Director("d1", "Director Test")));

        client.writeMovies(List.of(movie));

        verify(mockSession, times(1)).writeTransaction(any());
        verify(mockTransaction, times(1)).run(anyString(), any(Map.class));
    }
}