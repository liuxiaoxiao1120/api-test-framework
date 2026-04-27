package model;

public class ApiCase {
    private String caseId;
    private String module;
    private String apiName;
    private String enabled;
    private String method;
    private String baseUrlKey;
    private String path;
    private String headersFile;
    private String requestFile;
    private String assertFile;
    private String businessAssert;
    private String extractFile;
    private long maxResponseTime;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBaseUrlKey() {
        return baseUrlKey;
    }

    public void setBaseUrlKey(String baseUrlKey) {
        this.baseUrlKey = baseUrlKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHeadersFile() {
        return headersFile;
    }

    public void setHeadersFile(String headersFile) {
        this.headersFile = headersFile;
    }

    public String getRequestFile() {
        return requestFile;
    }

    public void setRequestFile(String requestFile) {
        this.requestFile = requestFile;
    }

    public String getAssertFile() {
        return assertFile;
    }

    public void setAssertFile(String assertFile) {
        this.assertFile = assertFile;
    }

    public String getBusinessAssert() {
        return businessAssert;
    }

    public void setBusinessAssert(String businessAssert) {
        this.businessAssert = businessAssert;
    }

    public String getExtractFile() {
        return extractFile;
    }

    public void setExtractFile(String extractFile) {
        this.extractFile = extractFile;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public boolean isEnabled() {
        return enabled != null && enabled.trim().equalsIgnoreCase("Y");
    }
}

