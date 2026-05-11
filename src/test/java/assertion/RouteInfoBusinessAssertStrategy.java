package assertion;

import model.ApiResponse;

final class RouteInfoBusinessAssertStrategy implements BusinessAssertStrategy {

    @Override
    public void assertBusiness(ApiResponse response) {
        RouteInfoAssert.assertRouteInfoBusiness(response);
    }
}
