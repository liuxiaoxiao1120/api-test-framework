package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static JsonNode readJsonNodeFromResource(String resourcePath) {
        try (InputStream in = openResource(resourcePath)) {
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON resource: " + resourcePath, e);
        }
    }

    public static String readTextFromResource(String resourcePath) {
        try (InputStream in = openResource(resourcePath)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read text resource: " + resourcePath, e);
        }
    }

    public static <T> T readFromResource(String resourcePath, Class<T> clazz) {
        try (InputStream in = openResource(resourcePath)) {
            return MAPPER.readValue(in, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON resource: " + resourcePath, e);
        }
    }

    public static <T> T readFromResource(String resourcePath, TypeReference<T> typeRef) {
        try (InputStream in = openResource(resourcePath)) {
            return MAPPER.readValue(in, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON resource: " + resourcePath, e);
        }
    }

    private static InputStream openResource(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalized);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return in;
    }

    /**
     * A minimal JSONPath-like getter for paths like:
     * $.a.b.c
     * $.a[0].b
     */
    public static JsonNode getByJsonPath(JsonNode root, String jsonPath) {
        if (root == null) {
            return MissingNode.getInstance();
        }
        if (jsonPath == null || jsonPath.isBlank()) {
            return MissingNode.getInstance();
        }
        String p = jsonPath.trim();
        if (!p.startsWith("$.")) {
            throw new IllegalArgumentException("Unsupported json path (must start with $.): " + jsonPath);
        }
        String expr = p.substring(2); // remove "$."
        JsonNode current = root;
        int i = 0;
        while (i < expr.length()) {
            int dot = findNextDotOutsideBracket(expr, i);
            String segment = dot == -1 ? expr.substring(i) : expr.substring(i, dot);
            current = applySegment(current, segment);
            if (dot == -1) {
                break;
            }
            i = dot + 1;
        }
        return current == null ? MissingNode.getInstance() : current;
    }

    private static int findNextDotOutsideBracket(String s, int from) {
        int bracket = 0;
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') bracket++;
            if (c == ']') bracket = Math.max(0, bracket - 1);
            if (c == '.' && bracket == 0) {
                return i;
            }
        }
        return -1;
    }

    private static JsonNode applySegment(JsonNode node, String segment) {
        if (node == null) {
            return MissingNode.getInstance();
        }
        String seg = segment;
        // fieldName[0][1]...
        int bracketIdx = seg.indexOf('[');
        String field = bracketIdx == -1 ? seg : seg.substring(0, bracketIdx);
        JsonNode cur = field.isEmpty() ? node : node.path(field);
        if (bracketIdx == -1) {
            return cur;
        }
        int i = bracketIdx;
        while (i < seg.length()) {
            if (seg.charAt(i) != '[') {
                break;
            }
            int end = seg.indexOf(']', i);
            if (end == -1) {
                throw new IllegalArgumentException("Invalid json path segment: " + segment);
            }
            String idxStr = seg.substring(i + 1, end).trim();
            int idx = Integer.parseInt(idxStr);
            cur = cur.path(idx);
            i = end + 1;
        }
        return cur;
    }

    public static Map<String, String> readStringMapFromResource(String resourcePath) {
        JsonNode node = readJsonNodeFromResource(resourcePath);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw new IllegalArgumentException("Headers JSON must be an object: " + resourcePath);
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            map.put(e.getKey(), e.getValue().isNull() ? null : e.getValue().asText());
        }
        return map;
    }
}

