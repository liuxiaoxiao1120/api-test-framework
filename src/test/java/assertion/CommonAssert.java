package assertion;

import com.fasterxml.jackson.databind.JsonNode;
import model.ApiCase;
import model.ApiResponse;
import org.assertj.core.api.Assertions;
import utils.JsonUtil;

import java.util.Iterator;
import java.util.Map;

public final class CommonAssert {
    private CommonAssert() {
    }

    public static void assertByRuleFile(ApiCase c, ApiResponse resp) {
        if (c.getAssertFile() == null || c.getAssertFile().isBlank()) {
            return;
        }
        JsonNode rule = JsonUtil.readJsonNodeFromResource(c.getAssertFile());
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

        if (rule.has("responseTimeLessThan")) {
            long max = rule.get("responseTimeLessThan").asLong();
            Assertions.assertThat(resp.getElapsedMs())
                    .as("response time (ms)")
                    .isLessThanOrEqualTo(max);
        }

        JsonNode json = resp.getJson();
        if (json == null || json.isNull() || json.isMissingNode()) {
            // if rules need json assertions, fail fast
            boolean needsJson = rule.has("jsonEquals") || rule.has("jsonNotEmpty")
                    || rule.has("jsonGreaterThan") || rule.has("jsonLessThan");
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
}

