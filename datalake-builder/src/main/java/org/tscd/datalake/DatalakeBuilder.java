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
        String filePath = localDirPath + "/movies.json";

        try {
            Files.createDirectories(new File(localDirPath).toPath());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(movieList);

            Files.writeString(new File(filePath).toPath(), jsonContent);

            System.out.println("Archivo JSON guardado localmente en: " + filePath);

        } catch (IOException e) {
            System.err.println("Error al escribir JSON: " + e.getMessage());
        }

        return filePath;
    }

    public void cloudStorage(String filePath) {
        client.createStorage();
        client.save(filePath);
    }

}
