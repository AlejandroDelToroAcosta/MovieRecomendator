package org.tscd.datalake;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.tscd.model.Movie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatalakeBuilder {
    private final StorageProvider client;

    public DatalakeBuilder(StorageProvider client) {
        this.client = client;
    }

    public String write(List<Movie> movieList) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String filePath = "datalake" + "/" + timestamp + "/movies.json";
        Path fullFilePath = Path.of(filePath);

        Path directoryPath = fullFilePath.getParent();

        try {
            cleanDirectory(Path.of("datalake"));
            Files.createDirectories(directoryPath);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(movieList);

            Files.writeString(fullFilePath, jsonContent);

            System.out.println("Archivo JSON guardado en: " + filePath);
            System.out.println("Versión de la corrida (timestamp): " + timestamp);

        } catch (IOException e) {
            System.err.println("Error al escribir JSON: " + e.getMessage());
        }

        return filePath;
    }

    public void cloudStorage(String filePath) {
        client.createStorage();
        client.save(filePath);
    }

    private void cleanDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        // Recorre el directorio en orden inverso (archivos, luego subdirectorios)
        Files.walk(path)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.err.println("Error al borrar el archivo o directorio: " + file);
                    }
                });
    }

}
