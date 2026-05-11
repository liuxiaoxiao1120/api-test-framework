package core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单条用例的执行摘要，记录是否通过、状态码、耗时和失败原因。
 *
 * <p>当前用于控制台输出；后续可扩展为平台化报告的数据源
 * （例如写入数据库、推送到 Allure 或飞书通知）。
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
