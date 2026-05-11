package sql;

import model.TestCase;

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
