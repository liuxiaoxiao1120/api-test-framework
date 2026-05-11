package utils;

import client.ApiHttpClient;
import client.OkHttpApiHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import config.ConfigLoader;
import context.VariableContext;
import execution.GlobalHeaders;

/**
 * Runs a single GET login before the suite and stores {@code token} in {@link VariableContext}.
 */
public final class LoginManager {

    private static final ApiHttpClient HTTP = new OkHttpApiHttpClient();

    private LoginManager() {
    }

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
        JsonNode codeNode = root.path("code");
        if (!codeNode.isMissingNode() && !codeNode.isNull()) {
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
}
