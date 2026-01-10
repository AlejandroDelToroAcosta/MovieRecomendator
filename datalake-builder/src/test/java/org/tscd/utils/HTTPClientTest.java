package org.tscd.utils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HTTPClientTest {

    @Test
    void testGet_returnsResponseBody() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"result\":\"ok\"}")
            );
            server.start();

            String url = server.url("/test").toString();
            String response = HTTPClient.get(url);

            assertEquals("{\"result\":\"ok\"}", response);
        }
    }

    @Test
    void testGet_throwsIOExceptionOnFailure() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(
                    new MockResponse().setResponseCode(500)
            );
            server.start();

            String url = server.url("/error").toString();

            assertThrows(IOException.class, () -> HTTPClient.get(url));
        }
    }
}
