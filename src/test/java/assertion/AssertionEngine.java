package assertion;

import com.fasterxml.jackson.databind.JsonNode;
import model.ApiResponse;
import model.TestCase;
import org.assertj.core.api.Assertions;
import utils.JsonUtil;

import java.util.Iterator;
import java.util.Map;

/**
 * Applies {@code assert.json} rules: status code, response time, JSON value / existence.
 */
public final class AssertionEngine {

    private AssertionEngine() {
    }

    public static void assertCase(TestCase testCase, ApiResponse resp) {
        if (!resourceExists(testCase.assertResourcePath())) {
            return;
        }
        JsonNode rule = JsonUtil.readJsonNodeFromResource(testCase.assertResourcePath());
        assertByRules(rule, resp);
    }

    public static void assertByRules(JsonNode rule, ApiResponse resp) {
        if (rule == null || rule.isNull() || rule.isMissingNode()) {
            return;
        }

        if (rule.has("statusCode")) {
            int expected = rule.get("statusCode").asInt();
            Assertions.assertThat(resp.getStatusCode())
                    .as("statusCode")
                    .isEqualTo(expected);
        }

        long timeCapMs = resolveResponseTimeCapMs(rule);
        if (timeCapMs > 0) {
            Assertions.assertThat(resp.getElapsedMs())
                    .as("response time (ms)")
                    .isLessThanOrEqualTo(timeCapMs);
        }

        JsonNode json = resp.getJson();
        if (json == null || json.isNull() || json.isMissingNode()) {
            boolean needsJson = rule.has("jsonEquals") || rule.has("jsonNotEmpty")
                    || rule.has("jsonExists") || rule.has("jsonGreaterThan") || rule.has("jsonLessThan");
            if (needsJson) {
                Assertions.fail("Response is not a valid JSON, but json assertions exist.");
            }
            return;
        }

        if (rule.has("jsonEquals")) {
            JsonNode equalsNode = rule.get("jsonEquals");
            if (!equalsNode.isObject()) {
                throw new IllegalArgumentException("jsonEquals must be an object");
            }
            Iterator<Map.Entry<String, JsonNode>> it = equalsNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String path = e.getKey();
                JsonNode expected = e.getValue();
                JsonNode actual = JsonUtil.getByJsonPath(json, path);
                assertJsonEquals(path, actual, expected);
            }
        }

        if (rule.has("jsonExists")) {
            JsonNode arr = rule.get("jsonExists");
            if (!arr.isArray()) {
                throw new IllegalArgumentException("jsonExists must be an array of JSON paths");
            }
            for (JsonNode p : arr) {
                String path = p.asText();
                JsonNode actual = JsonUtil.getByJsonPath(json, path);
                Assertions.assertThat(actual.isMissingNode())
                        .as("jsonExists %s", path)
                        .isFalse();
            }
        }

        if (rule.has("jsonNotEmpty")) {
            JsonNode arr = rule.get("jsonNotEmpty");
            if (!arr.isArray()) {
                throw new IllegalArgumentException("jsonNotEmpty must be an array");
            }
            for (JsonNode p : arr) {
                String path = p.asText();
                JsonNode actual = JsonUtil.getByJsonPath(json, path);
                Assertions.assertThat(isNotEmpty(actual))
                        .as("jsonNotEmpty %s", path)
                        .isTrue();
            }
        }

        if (rule.has("jsonGreaterThan")) {
            JsonNode gt = rule.get("jsonGreaterThan");
            if (!gt.isObject()) {
                throw new IllegalArgumentException("jsonGreaterThan must be an object");
            }
            Iterator<Map.Entry<String, JsonNode>> it = gt.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String path = e.getKey();
                double expected = e.getValue().asDouble();
                double actual = JsonUtil.getByJsonPath(json, path).asDouble();
                Assertions.assertThat(actual)
                        .as("jsonGreaterThan %s", path)
                        .isGreaterThan(expected);
            }
        }

        if (rule.has("jsonLessThan")) {
            JsonNode lt = rule.get("jsonLessThan");
            if (!lt.isObject()) {
                throw new IllegalArgumentException("jsonLessThan must be an object");
            }
            Iterator<Map.Entry<String, JsonNode>> it = lt.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String path = e.getKey();
                double expected = e.getValue().asDouble();
                double actual = JsonUtil.getByJsonPath(json, path).asDouble();
                Assertions.assertThat(actual)
                        .as("jsonLessThan %s", path)
                        .isLessThan(expected);
            }
        }
    }

    private static long resolveResponseTimeCapMs(JsonNode rule) {
        long cap = 0;
        if (rule.has("responseTimeLessThan")) {
            cap = rule.get("responseTimeLessThan").asLong();
        }
        if (rule.has("responseTimeMsMax")) {
            long alt = rule.get("responseTimeMsMax").asLong();
            cap = cap == 0 ? alt : Math.min(cap, alt);
        }
        return cap;
    }

    private static void assertJsonEquals(String path, JsonNode actual, JsonNode expected) {
        if (expected == null || expected.isNull()) {
            Assertions.assertThat(actual.isNull() || actual.isMissingNode())
                    .as("jsonEquals %s", path)
                    .isTrue();
            return;
        }
        if (expected.isNumber()) {
            Assertions.assertThat(actual.asDouble())
                    .as("jsonEquals %s", path)
                    .isEqualTo(expected.asDouble());
        } else if (expected.isBoolean()) {
            Assertions.assertThat(actual.asBoolean())
                    .as("jsonEquals %s", path)
                    .isEqualTo(expected.asBoolean());
        } else if (expected.isTextual()) {
            Assertions.assertThat(actual.asText())
                    .as("jsonEquals %s", path)
                    .isEqualTo(expected.asText());
        } else {
            Assertions.assertThat(actual)
                    .as("jsonEquals %s (json)", path)
                    .isEqualTo(expected);
        }
    }

    private static boolean isNotEmpty(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) {
            return false;
        }
        if (n.isArray() || n.isObject()) {
            return n.size() > 0;
        }
        if (n.isTextual()) {
            return !n.asText().isBlank();
        }
        return true;
    }

    private static boolean resourceExists(String classpathRelative) {
        if (classpathRelative == null || classpathRelative.isBlank()) {
            return false;
        }
        String n = classpathRelative.startsWith("/") ? classpathRelative.substring(1) : classpathRelative;
        return Thread.currentThread().getContextClassLoader().getResource(n) != null;
    }
}
