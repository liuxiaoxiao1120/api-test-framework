package core.client;

import java.util.Map;

/**
 * HTTP 客户端抽象接口：隔离底层 HTTP 库，使测试代码不依赖具体实现。
 *
 * <p>当前实现：{@link OkHttpApiHttpClient}（基于 OkHttp）。
 * 后续若需要 Mock、录制回放等能力，只需实现本接口并替换注入即可。
 */
public interface ApiHttpClient {

    HttpResult execute(String method, String url, Map<String, String> headers, String body);

    record HttpResult(int statusCode, String body, long elapsedMs) {
    }
}
