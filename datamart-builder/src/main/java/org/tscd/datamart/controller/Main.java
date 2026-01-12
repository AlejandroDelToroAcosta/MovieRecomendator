package org.tscd.datamart.controller;


import io.github.cdimascio.dotenv.Dotenv;
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
        Dotenv dotenv = Dotenv.load();
        String boltUrl = dotenv.get("BOLT_URL");
        String neo4jUser = dotenv.get("NEO4J_USER");
        String neo4jPasswd = dotenv.get("NEO4J_PASSWD");
        String sqsQueueUrl = dotenv.get("SQS_QUEUE_URL");

        StorageConsumer storageConsumer = new S3Consumer();

        DatalakeConsumerService datalakeService =
                new DatalakeConsumerService(storageConsumer);

        InsertionService insertionService =
                new Neo4jClient(boltUrl, neo4jUser, neo4jPasswd);

        MessageHandler handler =
                new MovieIngestionHandler(datalakeService, insertionService);

        QueueConsumer consumer =
                new SQSQueueConsumer(sqsQueueUrl
                        ,
                        handler
                );

        consumer.listen();

    }
}