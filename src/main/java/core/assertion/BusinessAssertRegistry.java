package core.assertion;

import core.model.ApiResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
