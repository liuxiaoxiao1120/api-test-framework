package core.assertion;

import core.model.ApiResponse;

final class RouteInfoBusinessAssertStrategy implements BusinessAssertStrategy {

    @Override
    public void assertBusiness(ApiResponse response) {
        RouteInfoAssert.assertRouteInfoBusiness(response);
    }
}
