package org.tscd.datamart.messaging;

import org.neo4j.bolt.connection.netty.impl.messaging.Message;

import java.util.List;

public interface QueueConsumer {
    void listen();
}
