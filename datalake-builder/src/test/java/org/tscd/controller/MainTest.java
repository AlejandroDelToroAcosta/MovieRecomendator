
package org.tscd.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.tscd.datalake.AmazonS3Provider;
import org.tscd.datalake.DatalakeBuilder;
import org.tscd.messaging.SQSQueuePublisher;
import org.tscd.model.Actor;
import org.tscd.model.Director;
import org.tscd.model.Movie;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MainTest {

    @Test
    void main_happyPath_runsWithMocks() throws Exception {
        List<String> ids = List.of("tt1");
        Movie fake = new Movie(
                "tt1",
                "Mocked Title",
                1954,
                List.of(new Actor("a1","Actor One")),
                List.of(new Director("d1","Dir One")),
                List.of("Drama"),
                7.0,
                5400
        );

        try (MockedConstruction<AmazonS3Provider> s3 = Mockito.mockConstruction(AmazonS3Provider.class);
             MockedConstruction<FilmProvider> films = Mockito.mockConstruction(FilmProvider.class,
                     (mock, ctx) -> {
                         when(mock.getMovieId()).thenReturn(ids);
                         when(mock.getMovieList(eq(ids))).thenReturn(List.of(fake));
                     });
             MockedConstruction<DatalakeBuilder> dl = Mockito.mockConstruction(DatalakeBuilder.class,
                     (mock, ctx) -> {
                         when(mock.write(any())).thenReturn("movies/file.json");
                     });
             MockedConstruction<SQSQueuePublisher> sqs = Mockito.mockConstruction(SQSQueuePublisher.class)
        ) {
            Main.main(new String[]{"my-bucket"});

            DatalakeBuilder dlMock = dl.constructed().get(0);
            verify(dlMock).write(any());
            verify(dlMock).cloudStorage("movies/file.json");

            SQSQueuePublisher publisher = sqs.constructed().get(0);
            verify(publisher).publish("s3://my-bucket/movies/file.json");
        }
    }

    @Test
    void main_withoutBucketArg_throwsIndexError() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> Main.main(new String[]{}));
    }
}
