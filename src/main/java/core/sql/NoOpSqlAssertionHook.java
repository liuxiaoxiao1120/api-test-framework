package core.sql;

import core.model.TestCase;

/**
 * {@link SqlAssertionHook} 的空实现：检测到 sql.sql 文件时打印提示，不执行实际 SQL 校验。
 * 在未接入数据库时作为默认实现使用。
 */
public final class NoOpSqlAssertionHook implements SqlAssertionHook {

    @Override
    public void runIfPresent(TestCase testCase) {
        if (!resourceExists(testCase.sqlResourcePath())) {
            return;
        }
        System.out.println("Note: sql.sql present for case " + testCase.getCaseId()
                + " — DB validation hook not wired (see sql.SqlAssertionHook).");
    }

    private static boolean resourceExists(String classpathRelative) {
        if (classpathRelative == null || classpathRelative.isBlank()) {
            return false;
        }
        String n = classpathRelative.startsWith("/") ? classpathRelative.substring(1) : classpathRelative;
        return Thread.currentThread().getContextClassLoader().getResource(n) != null;
    }
}
