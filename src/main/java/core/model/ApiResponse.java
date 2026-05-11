package core.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiResponse {
    private final int statusCode;
    private final String body;
    private final long elapsedMs;
    private final JsonNode json;

    public ApiResponse(int statusCode, String body, long elapsedMs, JsonNode json) {
        this.statusCode = statusCode;
        this.body = body;
        this.elapsedMs = elapsedMs;
        this.json = json;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public JsonNode getJson() {
        return json;
    }
}
