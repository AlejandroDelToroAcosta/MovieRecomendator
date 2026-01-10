package org.tscd.datamart.provider;

import org.junit.jupiter.api.Test;
import org.tscd.datamart.model.Movie;

import java.util.List;

import static org.mockito.Mockito.*;

class DatamartGraphpProviderTest {

    @Test
    void writeCall_callsInsertionService() {
        InsertionService mockService = mock(InsertionService.class);
        DatamartGraphpProvider provider = new DatamartGraphpProvider(mockService);

        Movie movie = new Movie();
        movie.setId("tt1");
        movie.setTitle("Movie 1");

        provider.writeCall(List.of(movie));

        verify(mockService, times(1)).writeMovies(List.of(movie));
    }
}
