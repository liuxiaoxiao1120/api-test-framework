package core.execution;

import core.config.ConfigLoader;
import core.context.VariableContext;
import core.model.TestCase;
import core.utils.JsonUtil;

import java.util.Map;

/**
 * 请求构建器：根据 {@link TestCase} 组装最终可执行的 HTTP 请求。
 *
 * <p>组装步骤：
 * <ol>
 *   <li>从 config.properties 按 {@code baseUrlKey} 查找 baseUrl，解析其中的占位符</li>
 *   <li>拼接 path，构造完整 URL</li>
 *   <li>调用 {@link GlobalHeaders#build()} 获取带 token 的全局请求头</li>
 *   <li>若 {@code request.json} 存在，读取并替换其中的占位符作为请求体</li>
 * </ol>
 */
public final class RequestBuilder {

    private RequestBuilder() {
    }

    /**
     * 根据用例构造请求，返回不可变的 {@link BuiltRequest}。
     */
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
