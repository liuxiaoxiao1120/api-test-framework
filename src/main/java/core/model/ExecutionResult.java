package core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Per-case execution summary for reporting and future platform export.
 */
public class ExecutionResult {
    private String caseId;
    private boolean passed;
    private int statusCode;
    private long elapsedMs;
    private String responseBody;
    private String failureReason;
    private final List<String> assertionMessages = new ArrayList<>();

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public List<String> getAssertionMessages() {
        return Collections.unmodifiableList(assertionMessages);
    }

    public void addAssertionMessage(String message) {
        if (message != null && !message.isBlank()) {
            assertionMessages.add(message);
        }
    }
}
