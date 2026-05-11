package execution;

import com.fasterxml.jackson.databind.JsonNode;
import config.ConfigLoader;
import context.VariableContext;
import utils.JsonUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Loads the global header template from classpath, resolves variables, and injects
 * {@code Authorization} when a token is present unless the template already defines it.
 */
public final class GlobalHeaders {

    private static final String DEFAULT_RESOURCE = "headers/global_headers.json";
    private static final String ACCEPT_JSON = "application/json, text/plain, */*";

    private GlobalHeaders() {
    }

    /** Headers for unauthenticated calls (e.g. login). No variable-based auth headers. */
    public static Map<String, String> buildLoginMinimal() {
        Map<String, String> m = new HashMap<>();
        m.put("Accept", ACCEPT_JSON);
        m.put("Content-Type", "application/json");
        return m;
    }

    public static Map<String, String> build() {
        String resource = ConfigLoader.getOptionalProperty("global.headers.file", DEFAULT_RESOURCE);
        Map<String, String> headers = readHeaderMap(resource);
        Map<String, String> resolved = new HashMap<>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) {
                continue;
            }
            String v = e.getValue();
            resolved.put(e.getKey(), v == null ? null : VariableContext.resolve(v));
        }
        injectAuthorizationIfNeeded(resolved);
        return resolved;
    }

    private static void injectAuthorizationIfNeeded(Map<String, String> headers) {
        boolean hasAuth = false;
        for (String k : headers.keySet()) {
            if (k != null && k.equalsIgnoreCase("Authorization")) {
                hasAuth = true;
                break;
            }
        }
        if (hasAuth) {
            return;
        }
        String token = VariableContext.get("token");
        if (token == null || token.isBlank()) {
            return;
        }
        headers.put("Authorization", "Bearer " + token.trim());
    }

    private static Map<String, String> readHeaderMap(String resourcePath) {
        JsonNode node = JsonUtil.readJsonNodeFromResource(resourcePath);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw new IllegalArgumentException("Global headers must be a JSON object: " + resourcePath);
        }
        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            JsonNode v = e.getValue();
            String text = v == null || v.isNull() ? null : v.asText();
            map.put(e.getKey(), text);
        }
        return map;
    }
}
