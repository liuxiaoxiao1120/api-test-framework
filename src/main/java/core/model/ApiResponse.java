package core.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 封装一次 HTTP 请求的响应结果，供断言引擎和提取引擎使用。
 *
 * <p>字段说明：
 * <ul>
 *   <li>{@code statusCode} — HTTP 状态码</li>
 *   <li>{@code body} — 原始响应体字符串</li>
 *   <li>{@code elapsedMs} — 请求耗时（毫秒），用于响应时间断言</li>
 *   <li>{@code json} — 已解析的 JSON 树，body 非 JSON 时为 null</li>
 * </ul>
 */
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
