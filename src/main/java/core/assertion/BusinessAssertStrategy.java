package core.assertion;

import core.model.ApiResponse;

/**
 * 业务断言策略接口：用于实现超出 assert.json 声明式范围的复杂业务校验。
 *
 * <p>使用方式：
 * <ol>
 *   <li>实现本接口，编写具体断言逻辑</li>
 *   <li>在 {@link BusinessAssertRegistry} 中注册策略名称</li>
 *   <li>在 CSV 用例的 {@code businessAssert} 列填写对应名称</li>
 * </ol>
 */
public interface BusinessAssertStrategy {

    /**
     * 执行业务级别的断言，失败时应抛出 {@link AssertionError}。
     */
    void assertBusiness(ApiResponse response);
}
