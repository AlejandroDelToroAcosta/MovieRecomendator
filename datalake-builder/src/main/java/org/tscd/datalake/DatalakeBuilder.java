package org.tscd.datalake;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.tscd.model.Movie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatalakeBuilder {
    private final StorageProvider client;

    public DatalakeBuilder(StorageProvider client) {
        this.client = client;
    }

    public String write(List<Movie> movieList) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String localDirPath = "datalake/" + timestamp;
        String filePath = localDirPath + "/movies.csv";

        try {
            Files.createDirectories(new File(localDirPath).toPath());
            try (
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                            .withHeader("ID", "Title", "Year", "Cast", "Directors", "Genre", "Rating", "Duration"))
            ) {
                for (Movie movie : movieList) {
                    csvPrinter.printRecord(
                            movie.getId(),
                            movie.getTitle(),
                            movie.getYear(),
                            movie.getCast(),
                            movie.getDirectors(),
                            movie.getGenre(),
                            movie.getRating(),
                            movie.getDuration()
                    );
                }
                csvPrinter.flush();
                System.out.println("Archivo CSV guardado localmente en: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("Error de E/S al escribir el CSV: " + e.getMessage());
        }
        return filePath;
    }

    public void cloudStorage(String filePath) {
        client.createStorage();
        client.save(filePath);
    }

}
