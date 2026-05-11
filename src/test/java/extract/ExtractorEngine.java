package extract;

import com.fasterxml.jackson.databind.JsonNode;
import context.VariableContext;
import model.ApiResponse;
import model.TestCase;
import utils.JsonUtil;

/**
 * Reads optional {@code extract.json} and stores values into {@link VariableContext}.
 */
public final class ExtractorEngine {

    private ExtractorEngine() {
    }

    public static void extractIfPresent(TestCase testCase, ApiResponse resp) {
        String path = testCase.extractResourcePath();
        if (!resourceExists(path)) {
            return;
        }
        JsonNode root = JsonUtil.readJsonNodeFromResource(path);
        if (root == null || root.isNull() || root.isMissingNode()) {
            return;
        }
        JsonNode vars = root.get("variables");
        if (vars == null || !vars.isObject()) {
            return;
        }
        JsonNode json = resp.getJson();
        if (json == null || json.isNull() || json.isMissingNode()) {
            throw new IllegalStateException("Cannot extract variables: response is not JSON");
        }
        var it = vars.fields();
        while (it.hasNext()) {
            var e = it.next();
            String varName = e.getKey();
            JsonNode pathNode = e.getValue();
            if (pathNode == null || !pathNode.isTextual()) {
                continue;
            }
            String jsonPath = pathNode.asText();
            JsonNode valueNode = JsonUtil.getByJsonPath(json, jsonPath);
            if (valueNode.isMissingNode() || valueNode.isNull()) {
                VariableContext.put(varName, "");
                continue;
            }
            String stored;
            if (valueNode.isTextual()) {
                stored = valueNode.asText();
            } else if (valueNode.isNumber() || valueNode.isBoolean()) {
                stored = valueNode.asText();
            } else {
                stored = valueNode.toString();
            }
            VariableContext.put(varName, stored);
        }
    }

    private static boolean resourceExists(String classpathRelative) {
        if (classpathRelative == null || classpathRelative.isBlank()) {
            return false;
        }
        String n = classpathRelative.startsWith("/") ? classpathRelative.substring(1) : classpathRelative;
        return Thread.currentThread().getContextClassLoader().getResource(n) != null;
    }
}
