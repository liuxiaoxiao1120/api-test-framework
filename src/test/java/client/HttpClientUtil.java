package client;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class HttpClientUtil {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;

    public HttpClientUtil() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }

    public HttpResponse postJson(String url, String jsonBody) {
        return request("POST", url, null, jsonBody);
    }

    public HttpResponse request(String method, String url, Map<String, String> headers, String body) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(url, "url");

        String m = method.trim().toUpperCase();
        Request.Builder builder = new Request.Builder().url(url);
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    builder.addHeader(e.getKey(), e.getValue());
                }
            }
        }

        RequestBody reqBody = null;
        if (body != null) {
            reqBody = RequestBody.create(body, JSON);
        }

        switch (m) {
            case "GET" -> builder.get();
            case "POST" -> builder.post(reqBody != null ? reqBody : RequestBody.create(new byte[0], JSON));
            case "PUT" -> builder.put(reqBody != null ? reqBody : RequestBody.create(new byte[0], JSON));
            case "DELETE" -> builder.delete(reqBody);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        Request request = builder.build();

        long startNs = System.nanoTime();
        try (Response response = client.newCall(request).execute()) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            String respBody = response.body() == null ? "" : response.body().string();

            return new HttpResponse(response.code(), respBody, elapsedMs);
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: " + url, e);
        }
    }

    public record HttpResponse(int statusCode, String body, long elapsedMs) {
    }
}

