package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class ConfigLoader {
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
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
        String value = getProperties().getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config property: " + key);
        }
        return value.trim();
    }

    public static String getOptionalProperty(String key, String defaultValue) {
        String value = getProperties().getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
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
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                if (in == null) {
                    throw new IllegalStateException("Config file not found on classpath: " + DEFAULT_CONFIG_FILE);
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config: " + DEFAULT_CONFIG_FILE, e);
            }
            cached = Objects.requireNonNull(props);
            return cached;
        }
    }
}
