package org.tscd.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HTTPClient {

    private static final OkHttpClient client = new OkHttpClient();

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .header("Connection", "keep-alive")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP code " + response.code() + " for URL: " + url);
            }
            return response.body().string();
        }
    }
}
