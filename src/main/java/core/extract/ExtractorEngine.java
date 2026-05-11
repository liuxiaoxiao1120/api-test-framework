package core.extract;

import com.fasterxml.jackson.databind.JsonNode;
import core.context.VariableContext;
import core.model.ApiResponse;
import core.model.TestCase;
import core.utils.JsonUtil;

/**
 * 响应提取引擎：读取用例目录下的 {@code extract.json}，
 * 按照 JSONPath 规则从响应中提取字段，并写入 {@link core.context.VariableContext}。
 *
 * <p>extract.json 格式示例：
 * <pre>
 * {
 *   "variables": {
 *     "orderId": "$.data.id",
 *     "status":  "$.data.status"
 *   }
 * }
 * </pre>
 * 提取后可在后续用例的请求体或路径中通过 {@code ${orderId}} 引用。
 *
 * <p>若 extract.json 不存在，本引擎直接跳过，不报错。
 */
public final class ExtractorEngine {

    private ExtractorEngine() {
    }

    /**
     * 若用例目录下存在 extract.json，则执行提取；否则静默跳过。
     */
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
