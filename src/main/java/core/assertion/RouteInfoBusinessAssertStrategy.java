package core.assertion;

import core.model.ApiResponse;

/**
 * 路线信息业务断言策略适配器，将 {@link RouteInfoAssert} 接入策略注册表。
 * 在 {@link BusinessAssertRegistry} 中注册为 "RouteInfoAssert"。
 */
final class RouteInfoBusinessAssertStrategy implements BusinessAssertStrategy {

    @Override
    public void assertBusiness(ApiResponse response) {
        RouteInfoAssert.assertRouteInfoBusiness(response);
    }
}
