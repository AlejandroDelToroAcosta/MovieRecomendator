package org.tscd.datamart.messaging;

import org.junit.jupiter.api.Test;
import org.tscd.datamart.consumer.DatalakeConsumerService;
import org.tscd.datamart.model.Movie;
import org.tscd.datamart.provider.InsertionService;

import java.util.List;

import static org.mockito.Mockito.*;

class MovieIngestionHandlerTest {

    @Test
    void handle_readsFromDatalakeAndWritesToInsertionService() throws Exception {
        DatalakeConsumerService datalakeService = mock(DatalakeConsumerService.class);
        InsertionService insertionService = mock(InsertionService.class);

        MovieIngestionHandler handler =
                new MovieIngestionHandler(datalakeService, insertionService);

        List<Movie> movies = List.of(new Movie());

        when(datalakeService.consumerCall("my-bucket", "movies/file.json"))
                .thenReturn(movies);

        String messageBody = "s3://my-bucket/movies/file.json";

        handler.handle(messageBody);

        verify(datalakeService, times(1))
                .consumerCall("my-bucket", "movies/file.json");

        verify(insertionService, times(1))
                .writeMovies(movies);
    }
}
