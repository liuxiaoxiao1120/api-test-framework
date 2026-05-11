package execution;

import config.ConfigLoader;
import context.VariableContext;
import model.TestCase;
import utils.JsonUtil;

import java.util.Map;

public final class RequestBuilder {

    private RequestBuilder() {
    }

    public static BuiltRequest build(TestCase tc) {
        String baseUrlKey = tc.getBaseUrlKey();
        if (baseUrlKey == null || baseUrlKey.isBlank()) {
            baseUrlKey = "apiBaseUrl";
        }
        String baseUrl = ConfigLoader.getRequiredProperty(baseUrlKey.trim());
        baseUrl = VariableContext.resolve(baseUrl);

        String path = tc.getPath() == null ? "" : tc.getPath().trim();
        String normalizedPath = path.startsWith("/") ? path : ("/" + path);
        String url = baseUrl.replaceAll("/+$", "") + normalizedPath;

        Map<String, String> headers = GlobalHeaders.build();

        String requestBody = null;
        if (resourceExists(tc.requestResourcePath())) {
            requestBody = JsonUtil.readTextFromResource(tc.requestResourcePath());
            requestBody = VariableContext.resolve(requestBody);
        }

        return new BuiltRequest(url, headers, requestBody);
    }

    private static boolean resourceExists(String classpathRelative) {
        if (classpathRelative == null || classpathRelative.isBlank()) {
            return false;
        }
        String n = classpathRelative.startsWith("/") ? classpathRelative.substring(1) : classpathRelative;
        return Thread.currentThread().getContextClassLoader().getResource(n) != null;
    }

    public record BuiltRequest(String url, Map<String, String> headers, String body) {
    }
}
