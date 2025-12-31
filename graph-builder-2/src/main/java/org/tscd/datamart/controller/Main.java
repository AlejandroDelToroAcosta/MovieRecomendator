package org.tscd.datamart.controller;


import org.tscd.datamart.messaging.MessageHandler;
import org.tscd.datamart.messaging.MovieIngestionHandler;
import org.tscd.datamart.messaging.QueueConsumer;
import org.tscd.datamart.messaging.SQSQueueConsumer;
import org.tscd.datamart.consumer.DatalakeConsumerService;
import org.tscd.datamart.provider.InsertionService;
import org.tscd.datamart.provider.Neo4jClient;
import org.tscd.datamart.consumer.S3Consumer;
import org.tscd.datamart.consumer.StorageConsumer;



public class Main {

    public static void main(String[] args) throws Exception {
        StorageConsumer storageConsumer = new S3Consumer();

        DatalakeConsumerService datalakeService =
                new DatalakeConsumerService(storageConsumer);

        InsertionService insertionService =
                new Neo4jClient(args[0], args[1], args[2]);

        MessageHandler handler =
                new MovieIngestionHandler(datalakeService, insertionService);

        QueueConsumer consumer =
                new SQSQueueConsumer(
                        "https://sqs.us-east-1.amazonaws.com/301998063112/movies-ingestion-queue",
                        handler
                );

        consumer.listen();

    }
}
