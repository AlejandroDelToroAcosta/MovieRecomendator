package org.tscd.datamart.messaging;

import org.tscd.datamart.consumer.DatalakeConsumerService;
import org.tscd.datamart.messaging.MessageHandler;
import org.tscd.datamart.model.Movie;
import org.tscd.datamart.provider.InsertionService;

import java.net.URI;
import java.util.List;

public class MovieIngestionHandler implements MessageHandler {

    private final DatalakeConsumerService datalakeService;
    private final InsertionService insertionService;

    public MovieIngestionHandler(DatalakeConsumerService datalakeService, InsertionService insertionService)
    {
        this.datalakeService = datalakeService;
        this.insertionService = insertionService;
    }

    @Override
    public void handle(String messageBody) throws Exception {
        URI s3Uri = new URI(messageBody);

        String bucket = s3Uri.getHost();
        String key = s3Uri.getPath().startsWith("/")
                ? s3Uri.getPath().substring(1)
                : s3Uri.getPath();

       // List<Movie> movies = datalakeService.consumerCall(bucket, key);
        //insertionService.writeMovies(movies);
    }
}