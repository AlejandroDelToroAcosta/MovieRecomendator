package org.tscd.datamart.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.tscd.datamart.consumer.S3Consumer;
import org.tscd.datamart.consumer.DatalakeConsumerService;
import org.tscd.datamart.messaging.MovieIngestionHandler;
import org.tscd.datamart.messaging.SQSQueueConsumer;
import org.tscd.datamart.provider.Neo4jClient;

import static org.mockito.Mockito.*;

class MainTest {

    @Test
    void main_wiresDependenciesAndStartsListening() throws Exception {

        try (
                MockedConstruction<S3Consumer> s3Mock = mockConstruction(S3Consumer.class);
                MockedConstruction<DatalakeConsumerService> datalakeMock =
                        mockConstruction(DatalakeConsumerService.class);
                MockedConstruction<Neo4jClient> neo4jMock =
                        mockConstruction(Neo4jClient.class);
                MockedConstruction<MovieIngestionHandler> handlerMock =
                        mockConstruction(MovieIngestionHandler.class);
                MockedConstruction<SQSQueueConsumer> sqsMock =
                        mockConstruction(
                                SQSQueueConsumer.class,
                                (mock, context) -> doNothing().when(mock).listen()
                        )
        ) {
            String[] args = {"bolt://localhost", "neo4j", "password"};

            Main.main(args);

            SQSQueueConsumer consumerInstance = sqsMock.constructed().get(0);
            verify(consumerInstance, times(1)).listen();
        }
    }
}
