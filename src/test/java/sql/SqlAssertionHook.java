package sql;

import model.TestCase;

/**
 * Optional database validation hook; default installations can no-op until JDBC is wired.
 */
public interface SqlAssertionHook {

    void runIfPresent(TestCase testCase);
}
