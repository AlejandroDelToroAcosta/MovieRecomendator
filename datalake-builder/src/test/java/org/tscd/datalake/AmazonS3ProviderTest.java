package org.tscd.datalake;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AmazonS3ProviderTest {

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new AmazonS3Provider("test-bucket"));
    }

    @Test
    void createStorage_doesNotThrow() {
        AmazonS3Provider provider = new AmazonS3Provider("test-bucket");

        assertDoesNotThrow(provider::createStorage);
    }
}
