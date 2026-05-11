package core.model;

/**
 * 统一用例模型：CSV 读取后封装为 ApiCase，作为测试执行的标准入口。
 * 通过 {@link #toTestCase()} 可转换为 {@link TestCase} 以兼容现有执行引擎。
 */
public class ApiCase {

    /** 用例编号，例如 RI001 */
    private String caseId;

    /** 用例名称（对应 CSV apiName 列）*/
    private String caseName;

    /** 所属模块（对应 CSV module 列）*/
    private String module;

    /** 请求方法，例如 GET、POST */
    private String method;

    /** 接口路径，例如 /infoRoadSection/getInfoRoadSectionListPage */
    private String path;

    /** 请求参数 JSON 文件的 classpath 路径，例如 cases/RI001_route_info/request.json */
    private String requestFile;

    /** 断言 JSON 文件的 classpath 路径，例如 cases/RI001_route_info/assert.json */
    private String assertFile;

    /** 是否启用本用例，false 时 DataProvider 会跳过 */
    private boolean enabled;

    /** 是否需要注入 token（预留，默认 false）*/
    private boolean needToken;

    /** 提取规则文件路径（预留，默认空字符串）*/
    private String extractFile;

    /** 前置用例编号（预留，默认空字符串）*/
    private String preCaseId;

    /** 最大响应时间（ms），0 表示不限制 */
    private Long maxResponseTime;

    // ---- 引擎兼容字段（对应现有 TestCase 字段）----

    /** baseUrl 配置 key，例如 apiBaseUrl（供 RequestBuilder 使用）*/
    private String baseUrlKey;

    /** 用例目录（classpath 相对路径），用于推导 request/assert/extract 路径 */
    private String caseDir;

    /** 业务断言类名（供 BusinessAssertRegistry 使用）*/
    private String businessAssert;

    // ---- getters / setters ----

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getCaseName() { return caseName; }
    public void setCaseName(String caseName) { this.caseName = caseName; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getRequestFile() { return requestFile; }
    public void setRequestFile(String requestFile) { this.requestFile = requestFile; }

    public String getAssertFile() { return assertFile; }
    public void setAssertFile(String assertFile) { this.assertFile = assertFile; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isNeedToken() { return needToken; }
    public void setNeedToken(boolean needToken) { this.needToken = needToken; }

    public String getExtractFile() { return extractFile; }
    public void setExtractFile(String extractFile) { this.extractFile = extractFile; }

    public String getPreCaseId() { return preCaseId; }
    public void setPreCaseId(String preCaseId) { this.preCaseId = preCaseId; }

    public Long getMaxResponseTime() { return maxResponseTime; }
    public void setMaxResponseTime(Long maxResponseTime) { this.maxResponseTime = maxResponseTime; }

    public String getBaseUrlKey() { return baseUrlKey; }
    public void setBaseUrlKey(String baseUrlKey) { this.baseUrlKey = baseUrlKey; }

    public String getCaseDir() { return caseDir; }
    public void setCaseDir(String caseDir) { this.caseDir = caseDir; }

    public String getBusinessAssert() { return businessAssert; }
    public void setBusinessAssert(String businessAssert) { this.businessAssert = businessAssert; }

    // ---- 转换方法 ----

    /**
     * 将 ApiCase 转换为 {@link TestCase}，以兼容现有 RequestBuilder / AssertionEngine 等执行引擎。
     */
    public TestCase toTestCase() {
        TestCase tc = new TestCase();
        tc.setCaseId(this.caseId);
        tc.setModule(this.module);
        tc.setApiName(this.caseName);
        tc.setEnabled(this.enabled ? "Y" : "N");
        tc.setMethod(this.method);
        tc.setBaseUrlKey(this.baseUrlKey);
        tc.setPath(this.path);
        tc.setCaseDir(this.caseDir);
        tc.setBusinessAssert(this.businessAssert);
        return tc;
    }

    @Override
    public String toString() {
        return "ApiCase{" +
                "caseId='" + caseId + '\'' +
                ", caseName='" + caseName + '\'' +
                ", module='" + module + '\'' +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", requestFile='" + requestFile + '\'' +
                ", assertFile='" + assertFile + '\'' +
                ", enabled=" + enabled +
                ", needToken=" + needToken +
                ", extractFile='" + extractFile + '\'' +
                ", preCaseId='" + preCaseId + '\'' +
                ", maxResponseTime=" + maxResponseTime +
                ", baseUrlKey='" + baseUrlKey + '\'' +
                ", caseDir='" + caseDir + '\'' +
                ", businessAssert='" + businessAssert + '\'' +
                '}';
    }
}
