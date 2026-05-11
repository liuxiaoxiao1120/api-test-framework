package core.assertion;

import core.model.ApiResponse;

/**
 * Optional interface-specific business checks beyond declarative {@code assert.json}.
 */
public interface BusinessAssertStrategy {

    void assertBusiness(ApiResponse response);
}
