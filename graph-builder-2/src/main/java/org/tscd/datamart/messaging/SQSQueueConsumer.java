package org.tscd.datamart.messaging;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.List;

import static software.amazon.awssdk.regions.Region.US_EAST_1;
public class SQSQueueConsumer implements QueueConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final MessageHandler handler;

    public SQSQueueConsumer(String queueUrl, MessageHandler handler) {
        this.queueUrl = queueUrl;
        this.handler = handler;
        this.sqsClient = SqsClient.builder()
                .region(US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Override
    public void listen() {
        System.out.println("Escuchando mensajes en: " + queueUrl);

        while (true) {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(1)
                    .waitTimeSeconds(20)
                    .build();

            for (Message msg : sqsClient.receiveMessage(request).messages()) {
                try {
                    handler.handle(msg.body());

                    sqsClient.deleteMessage(b -> b
                            .queueUrl(queueUrl)
                            .receiptHandle(msg.receiptHandle())
                    );

                } catch (Exception e) {
                    System.err.println("Error procesando mensaje: " + e.getMessage());
                }
            }
        }
    }

    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }
}
