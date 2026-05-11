package context;

import utils.VariableUtil;

import java.util.Map;

/**
 * Variable pool for {@code ${name}} substitution in URLs, bodies, and headers.
 * Backed by {@link VariableUtil} for gradual migration.
 */
public final class VariableContext {

    private VariableContext() {
    }

    public static void put(String name, String value) {
        VariableUtil.set(name, value);
    }

    public static String get(String name) {
        return VariableUtil.get(name);
    }

    public static String resolve(String text) {
        return VariableUtil.resolve(text);
    }

    public static String resolve(String text, Map<String, String> extra) {
        return VariableUtil.resolve(text, extra);
    }
}
