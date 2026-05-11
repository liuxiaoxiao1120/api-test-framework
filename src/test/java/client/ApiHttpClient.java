package client;

import java.util.Map;

/**
 * HTTP facade for tests; implementations must not leak OkHttp types to callers.
 */
public interface ApiHttpClient {

    HttpResult execute(String method, String url, Map<String, String> headers, String body);

    record HttpResult(int statusCode, String body, long elapsedMs) {
    }
}
