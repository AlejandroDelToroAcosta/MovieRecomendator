package org.tscd.datamart.messaging;

public interface MessageHandler {
    void handle(String messageBody) throws Exception;
}