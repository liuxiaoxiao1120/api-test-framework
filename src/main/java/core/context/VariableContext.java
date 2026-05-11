package core.context;

import core.config.ConfigLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 运行时变量池，负责在测试用例之间传递动态值（如 token、提取的响应字段）。
 *
 * <p>核心用途：
 * <ul>
 *   <li>登录后将 token 写入：{@code VariableContext.put("token", value)}</li>
 *   <li>RequestBuilder / GlobalHeaders 在组装请求时替换 {@code ${token}} 等占位符</li>
 *   <li>ExtractorEngine 将响应字段提取后写入，供后续用例通过 {@code ${varName}} 引用</li>
 * </ul>
 *
 * <p>占位符语法：{@code ${varName}}，额外支持：
 * <ul>
 *   <li>{@code ${env:VAR_NAME}} — 读取环境变量</li>
 *   <li>{@code ${sys:prop.name}} — 读取 JVM 系统属性</li>
 *   <li>{@code ${baseUrl}} — 从配置文件读取 apiBaseUrl</li>
 * </ul>
 *
 * <p>线程安全：底层使用 {@link ConcurrentHashMap}，支持并发写入。
 */
public final class VariableContext {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static final ConcurrentMap<String, String> STORE = new ConcurrentHashMap<>();

    private VariableContext() {
    }

    /**
     * 写入一个变量。value 为 null 时等同于删除该变量。
     */
    public static void put(String name, String value) {
        if (name == null || name.isBlank()) {
            return;
        }
        if (value == null) {
            STORE.remove(name.trim());
            return;
        }
        STORE.put(name.trim(), value);
    }

    /**
     * 读取一个变量，不存在时返回 null。
     */
    public static String get(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return STORE.get(name.trim());
    }

    /**
     * 将文本中所有 {@code ${varName}} 占位符替换为实际值。
     * 未找到的占位符替换为空字符串。
     */
    public static String resolve(String text) {
        return resolve(text, Map.of());
    }

    /**
     * 将文本中所有占位符替换为实际值。
     * extra 中的变量优先级高于全局变量池。
     */
    public static String resolve(String text, Map<String, String> extra) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Matcher m = PLACEHOLDER.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1).trim();
            String value = lookup(key, extra);
            m.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 按优先级查找变量值：extra → 全局 STORE → 内置特殊前缀 → 配置文件。
     */
    private static String lookup(String key, Map<String, String> extra) {
        if (extra != null && extra.containsKey(key)) {
            return extra.get(key);
        }
        if (STORE.containsKey(key)) {
            return STORE.get(key);
        }
        // 兼容旧占位符 ${baseUrl}
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
