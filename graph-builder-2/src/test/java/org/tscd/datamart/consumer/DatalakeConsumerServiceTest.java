package org.tscd.datamart.consumer;

import org.junit.jupiter.api.Test;
import org.tscd.datamart.model.Movie;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatalakeConsumerServiceTest {

    @Test
    void consumerCall_returnsMovieList() throws Exception {
        StorageConsumer consumer = mock(StorageConsumer.class);

        Movie movie = new Movie();
        movie.setId("tt1");
        movie.setTitle("Movie 1");
        movie.setYear(2000);

        List<Movie> movies = List.of(movie);

        when(consumer.get("bucket", "key")).thenReturn(movies);

        DatalakeConsumerService service = new DatalakeConsumerService(consumer);

        List<Movie> result = service.consumerCall("bucket", "key");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tt1", result.get(0).getId());

        verify(consumer).get("bucket", "key");
    }
}
