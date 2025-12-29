package org.tscd.messaging;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

public class SQSQueuePublisher implements QueuePublisher{
    private final SqsClient sqsClient;
    private final String queueUrl;

    public SQSQueuePublisher(String queueUrl) {
        this.queueUrl = queueUrl;
        this.sqsClient =  SqsClient.builder()
                .region(US_EAST_1)
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Override
    public void publish(String message) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();

        sqsClient.sendMessage(request);
        System.out.println("Mensaje" + message +"publicado");
    }

    private static AwsCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }
}
