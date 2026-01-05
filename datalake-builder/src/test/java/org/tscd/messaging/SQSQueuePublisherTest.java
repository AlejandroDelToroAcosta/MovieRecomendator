package org.tscd.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.mockito.Mockito.*;

class SQSQueuePublisherTest {

    private SqsClient mockSqsClient;
    private SQSQueuePublisher publisher;

    @BeforeEach
    void setUp() {
        mockSqsClient = mock(SqsClient.class);

        publisher = new SQSQueuePublisher("https://fake-queue-url", mockSqsClient);
    }

    @Test
    void testPublish_callsSqsClientSendMessage() {
        String message = "Hello World";

        publisher.publish(message);

        verify(mockSqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }
}
