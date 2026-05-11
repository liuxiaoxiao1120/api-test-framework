package core.model;

/**
 * CSV 用例清单中的一行数据，同时提供各类资源文件的 classpath 路径推导。
 *
 * <p>该类是执行引擎（RequestBuilder / AssertionEngine / ExtractorEngine）
 * 使用的内部模型。外部入口使用 {@link ApiCase}，通过 {@link ApiCase#toTestCase()} 转换而来。
 *
 * <p>路径推导规则：所有 JSON 资源文件均位于 {@link #caseDir} 目录下，
 * 例如 {@code cases/RI001_route_info/request.json}。
 */
public class TestCase {
    private String caseId;
    private String module;
    private String apiName;
    private String enabled;
    private String method;
    private String baseUrlKey;
    private String path;
    /** Classpath directory, e.g. {@code cases/RI001_route_info}. */
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
