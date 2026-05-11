package core.execution;

import com.fasterxml.jackson.databind.JsonNode;
import core.client.ApiHttpClient;
import core.client.OkHttpApiHttpClient;
import core.config.ConfigLoader;
import core.context.VariableContext;
import core.utils.JsonUtil;

/**
 * 登录执行器：在测试套件启动前完成一次登录，并将 token 写入 {@link VariableContext}。
 *
 * <p>执行流程：
 * <ol>
 *   <li>从 config.properties 读取 loginBaseUrl 和 loginPath，拼接登录 URL</li>
 *   <li>发送 GET 请求（携带用户名 + 密码作为 URL 参数）</li>
 *   <li>校验响应 code == 1000，从 {@code $.data} 提取 token</li>
 *   <li>调用 {@code VariableContext.put("token", token)}，供后续请求通过 {@code ${token}} 占位符引用</li>
 * </ol>
 *
 * <p>config.properties 中需要配置：
 * <pre>
 *   loginBaseUrl=http://host:port
 *   loginPath=/your/login/path?userName=xxx&amp;password=yyy
 * </pre>
 */
public final class LoginManager {

    private static final ApiHttpClient HTTP = new OkHttpApiHttpClient();

    private LoginManager() {
    }

    /**
     * 执行登录并将 token 存入 {@link VariableContext}。
     * 登录失败时抛出 {@link RuntimeException}，阻断整个测试套件。
     */
    public static void login() throws Exception {
        String baseUrl = VariableContext.resolve(ConfigLoader.getRequiredProperty("loginBaseUrl").trim());
        String path = ConfigLoader.getRequiredProperty("loginPath").trim();
        path = VariableContext.resolve(path);
        String normalizedPath = path.startsWith("/") ? path : ("/" + path);
        String url = baseUrl.replaceAll("/+$", "") + normalizedPath;

        ApiHttpClient.HttpResult raw = HTTP.execute("GET", url, GlobalHeaders.buildLoginMinimal(), null);

        if (raw.statusCode() != 200) {
            throw new RuntimeException("Login failed: HTTP " + raw.statusCode() + ", body=" + raw.body());
        }

        String body = raw.body() == null ? "" : raw.body();
        if (body.isBlank()) {
            throw new RuntimeException("Login failed: empty response body");
        }

        JsonNode root = JsonUtil.mapper().readTree(body);
        validateResponseCode(root);

        JsonNode dataNode = JsonUtil.getByJsonPath(root, "$.data");
        if (dataNode == null || dataNode.isMissingNode() || dataNode.isNull()) {
            throw new RuntimeException("Login failed: $.data is null or missing");
        }

        String token;
        if (dataNode.isTextual()) {
            token = dataNode.asText();
        } else if (dataNode.isNumber()) {
            token = dataNode.asText();
        } else {
            token = dataNode.toString();
        }
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Login failed: token extracted from $.data is blank");
        }

        VariableContext.put("token", token.trim());
    }

    /**
     * 校验响应中的 code 字段：仅当 code 存在且不为 1000 时抛出异常。
     */
    private static void validateResponseCode(JsonNode root) {
        JsonNode codeNode = root.path("code");
        if (codeNode.isMissingNode() || codeNode.isNull()) {
            return;
        }
        Integer code = null;
        if (codeNode.isIntegralNumber()) {
            code = codeNode.intValue();
        } else if (codeNode.isTextual()) {
            String t = codeNode.asText().trim();
            if (!t.isEmpty()) {
                try {
                    code = Integer.parseInt(t);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Login failed: invalid code in response: " + t, e);
                }
            }
        }
        if (code != null && code != 1000) {
            String msg = root.path("msg").asText("");
            throw new RuntimeException("Login failed: code=" + code + ", msg=" + msg);
        }
    }
}
