package core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class ConfigLoader {
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String LOCAL_OVERLAY_FILE = "config.local.properties";
    private static volatile Properties cached;

    private ConfigLoader() {
    }

    public static String getBaseUrl() {
        // Backward compatibility for ${baseUrl}. Default to apiBaseUrl.
        return getRequiredProperty("apiBaseUrl");
    }

    public static String getLoginBaseUrl() {
        return getRequiredProperty("loginBaseUrl");
    }

    public static String getApiBaseUrl() {
        return getRequiredProperty("apiBaseUrl");
    }

    public static String getRequiredProperty(String key) {
        String value = resolveProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config property: " + key);
        }
        return value.trim();
    }

    public static String getOptionalProperty(String key, String defaultValue) {
        String value = resolveProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Effective value: {@code -Dkey=...}, then {@code ENV_KEY} (camelCase to SCREAMING_SNAKE), then merged properties file.
     */
    private static String resolveProperty(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        String env = System.getenv(toEnvKey(key));
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return getProperties().getProperty(key);
    }

    private static String toEnvKey(String camelKey) {
        if (camelKey == null || camelKey.isEmpty()) {
            return camelKey;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelKey.length(); i++) {
            char c = camelKey.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    private static Properties getProperties() {
        if (cached != null) {
            return cached;
        }
        synchronized (ConfigLoader.class) {
            if (cached != null) {
                return cached;
            }
            Properties props = new Properties();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try (InputStream in = cl.getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                if (in == null) {
                    throw new IllegalStateException("Config file not found on classpath: " + DEFAULT_CONFIG_FILE);
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config: " + DEFAULT_CONFIG_FILE, e);
            }
            try (InputStream local = cl.getResourceAsStream(LOCAL_OVERLAY_FILE)) {
                if (local != null) {
                    props.load(local);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config overlay: " + LOCAL_OVERLAY_FILE, e);
            }
            cached = Objects.requireNonNull(props);
            return cached;
        }
    }
}
