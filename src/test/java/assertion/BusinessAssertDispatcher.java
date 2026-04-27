package assertion;

import model.ApiCase;
import model.ApiResponse;

public final class BusinessAssertDispatcher {
    private BusinessAssertDispatcher() {
    }

    public static void dispatch(ApiCase c, ApiResponse resp) {
        String name = c.getBusinessAssert();
        if (name == null || name.isBlank()) {
            return;
        }
        String key = name.trim();
        switch (key) {
            case "RouteInfoAssert" -> RouteInfoAssert.assertRouteInfoBusiness(resp);
            default -> throw new IllegalArgumentException("Unknown businessAssert: " + key);
        }
    }
}

