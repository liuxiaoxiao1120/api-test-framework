package utils;

import config.ConfigLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very small variable resolver.
 * Supports ${baseUrl}, ${env:XXX}, ${sys:XXX}.
 */
public final class VariableUtil {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final ConcurrentMap<String, String> GLOBALS = new ConcurrentHashMap<>();

    private VariableUtil() {
    }

    public static void set(String key, String value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (value == null) {
            GLOBALS.remove(key.trim());
            return;
        }
        GLOBALS.put(key.trim(), value);
    }

    public static String get(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return GLOBALS.get(key.trim());
    }

    public static String resolve(String text) {
        return resolve(text, Map.of());
    }

    public static String resolve(String text, Map<String, String> extra) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Matcher m = PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1).trim();
            String value = lookup(key, extra);
            if (value == null) {
                value = "";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String lookup(String key, Map<String, String> extra) {
        if (extra != null && extra.containsKey(key)) {
            return extra.get(key);
        }
        if (GLOBALS.containsKey(key)) {
            return GLOBALS.get(key);
        }
        if ("baseUrl".equals(key)) {
            return ConfigLoader.getBaseUrl();
        }
        if (key.startsWith("env:")) {
            return System.getenv(key.substring("env:".length()));
        }
        if (key.startsWith("sys:")) {
            return System.getProperty(key.substring("sys:".length()));
        }
        return null;
    }
}

