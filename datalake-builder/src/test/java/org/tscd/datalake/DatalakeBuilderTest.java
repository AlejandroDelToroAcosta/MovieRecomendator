package org.tscd.datalake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tscd.model.Actor;
import org.tscd.model.Director;
import org.tscd.model.Movie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatalakeBuilderTest {

    private StorageProvider mockStorageProvider;
    private DatalakeBuilder datalakeBuilder;

    @BeforeEach
    void setUp() {
        mockStorageProvider = mock(StorageProvider.class);
        datalakeBuilder = new DatalakeBuilder(mockStorageProvider);
    }

    @Test
    void testWrite_createsJsonFile() throws IOException {
        Movie movie = new Movie("tt0001", "Test Movie", 2020,
                List.of(new Actor("a1", "Actor One")),
                List.of(new Director("d1", "Director One")),
                List.of("Action"), 8.5, 7200);

        List<Movie> movies = List.of(movie);
        String filePath = datalakeBuilder.write(movies);

        Path path = Path.of(filePath);
        assertTrue(Files.exists(path));

        String content = Files.readString(path);
        assertTrue(content.contains("Test Movie"));

        Files.deleteIfExists(path);
        Files.deleteIfExists(path.getParent());
        Files.deleteIfExists(path.getParent().getParent());
    }

    @Test
    void testCloudStorage_callsStorageProvider() {
        String fakePath = "fake/path/movies.json";
        datalakeBuilder.cloudStorage(fakePath);

        verify(mockStorageProvider, times(1)).createStorage();
        verify(mockStorageProvider, times(1)).save(fakePath);
    }
}
