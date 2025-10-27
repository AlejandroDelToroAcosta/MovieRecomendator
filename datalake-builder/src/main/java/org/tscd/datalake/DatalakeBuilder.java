package org.tscd.datalake;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.tscd.model.Movie;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DatalakeBuilder {

    public void write(List<Movie> movieList, String filePath) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
