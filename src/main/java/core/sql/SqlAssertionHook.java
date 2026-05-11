package core.sql;

import core.model.TestCase;

/**
 * 数据库校验钩子接口（预留扩展点）。
 *
 * <p>当接口返回 HTTP 200 但需要额外校验数据库状态时（如订单是否落库），
 * 可实现本接口并在用例目录下放置 {@code sql.sql} 文件。
 * 当前默认实现为 {@link NoOpSqlAssertionHook}（不做任何操作）。
 *
 * <p>后续接入 JDBC 时，替换注入实现类即可，无需修改调用方。
 */
public interface SqlAssertionHook {

    /**
     * 若用例目录下存在 sql.sql，执行数据库校验；否则静默跳过。
     */
    void runIfPresent(TestCase testCase);
}
