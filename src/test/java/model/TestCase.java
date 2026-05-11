package model;

/**
 * One row from the case manifest CSV plus resolved artifact paths under {@link #getCaseDir()}.
 */
public class TestCase {
    private String caseId;
    private String module;
    private String apiName;
    private String enabled;
    private String method;
    private String baseUrlKey;
    private String path;
    /** Classpath directory relative to test resources, e.g. {@code cases/RI001_route_info}. */
    private String caseDir;
    private String businessAssert;

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

    public String getCaseDir() {
        return caseDir;
    }

    public void setCaseDir(String caseDir) {
        this.caseDir = caseDir;
    }

    public String getBusinessAssert() {
        return businessAssert;
    }

    public void setBusinessAssert(String businessAssert) {
        this.businessAssert = businessAssert;
    }

    public boolean isEnabled() {
        if (enabled == null) {
            return false;
        }
        String e = enabled.trim();
        return e.equalsIgnoreCase("Y")
                || e.equalsIgnoreCase("yes")
                || e.equalsIgnoreCase("true")
                || e.equals("1");
    }

    public String requestResourcePath() {
        return joinCaseDir("request.json");
    }

    public String assertResourcePath() {
        return joinCaseDir("assert.json");
    }

    public String extractResourcePath() {
        return joinCaseDir("extract.json");
    }

    public String sqlResourcePath() {
        return joinCaseDir("sql.sql");
    }

    private String joinCaseDir(String fileName) {
        String dir = caseDir == null ? "" : caseDir.trim();
        if (dir.isEmpty()) {
            return fileName;
        }
        String slash = dir.endsWith("/") ? "" : "/";
        return dir + slash + fileName;
    }
}
