package core.assertion;

import core.model.ApiResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务断言策略注册表：管理接口专属的业务逻辑断言。
 *
 * <p>当 assert.json 的通用规则无法覆盖复杂业务逻辑时（如跨字段联合校验），
 * 可实现 {@link BusinessAssertStrategy} 接口，并在此注册。
 *
 * <p>CSV 用例的 {@code businessAssert} 列填写策略名称（如 {@code RouteInfoAssert}），
 * 框架在执行时会自动调用对应策略。
 */
public final class BusinessAssertRegistry {

    private static final Map<String, BusinessAssertStrategy> STRATEGIES = new ConcurrentHashMap<>();

    static {
        register("RouteInfoAssert", new RouteInfoBusinessAssertStrategy());
    }

    private BusinessAssertRegistry() {
    }

    public static void register(String name, BusinessAssertStrategy strategy) {
        if (name == null || name.isBlank() || strategy == null) {
            return;
        }
        STRATEGIES.put(name.trim(), strategy);
    }

    public static void run(String strategyName, ApiResponse response) {
        if (strategyName == null || strategyName.isBlank()) {
            return;
        }
        BusinessAssertStrategy s = STRATEGIES.get(strategyName.trim());
        if (s == null) {
            throw new IllegalArgumentException("Unknown businessAssert strategy: " + strategyName);
        }
        s.assertBusiness(response);
    }
}
