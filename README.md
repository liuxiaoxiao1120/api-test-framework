# api-test — Java + TestNG 接口自动化测试框架

## 框架定位

基于 **Java 17 + TestNG + OkHttp** 的接口自动化测试框架，采用"数据驱动"模式：
- 用例配置写在 **CSV** 文件里，不需要改代码即可新增接口用例
- 请求参数、断言规则、提取规则分别放在独立的 **JSON** 文件里
- 支持运行时变量（token 自动注入、字段提取后跨用例传递）

---

## 目录结构

```
api-test/
├── pom.xml                              Maven 构建配置
├── testng.xml                           TestNG 套件入口
│
├── src/main/java/
│   └── core/
│       ├── model/                       数据模型层
│       │   ├── ApiCase.java             外部用例模型（CSV → ApiCase）
│       │   ├── TestCase.java            引擎内部用例模型（ApiCase.toTestCase()）
│       │   ├── ApiResponse.java         HTTP 响应封装
│       │   └── ExecutionResult.java     单条用例执行结果摘要
│       │
│       ├── loader/                      用例加载层
│       │   └── CaseLoader.java          读取 CSV → List<ApiCase>
│       │
│       ├── config/                      配置读取层
│       │   └── ConfigLoader.java        读取 config.properties，支持 -D 和环境变量覆盖
│       │
│       ├── context/                     运行时变量池
│       │   └── VariableContext.java     存取 ${varName} 占位符变量（token 等）
│       │
│       ├── execution/                   请求执行层
│       │   ├── LoginManager.java        登录执行，将 token 写入 VariableContext
│       │   ├── RequestBuilder.java      组装 URL / Header / Body
│       │   └── GlobalHeaders.java       全局请求头构建，自动注入 token
│       │
│       ├── extract/                     响应提取层
│       │   └── ExtractorEngine.java     按 extract.json 从响应提取字段
│       │
│       ├── assertion/                   断言层
│       │   ├── AssertionEngine.java     按 assert.json 执行声明式断言
│       │   ├── BusinessAssertStrategy.java   业务断言接口
│       │   ├── BusinessAssertRegistry.java   业务断言策略注册表
│       │   ├── RouteInfoAssert.java     路线信息接口的业务断言实现
│       │   └── RouteInfoBusinessAssertStrategy.java  策略适配器
│       │
│       ├── client/                      HTTP 客户端层
│       │   ├── ApiHttpClient.java       HTTP 客户端接口（抽象，便于 Mock）
│       │   └── OkHttpApiHttpClient.java OkHttp 实现
│       │
│       ├── sql/                         数据库校验钩子（预留）
│       │   ├── SqlAssertionHook.java    DB 校验接口
│       │   └── NoOpSqlAssertionHook.java 空实现（当前默认）
│       │
│       └── utils/                       通用工具
│           └── JsonUtil.java            Jackson 封装 + 轻量 JSONPath
│
├── src/main/resources/
│   ├── config.properties                环境配置（baseUrl、loginPath 等）
│   ├── headers/
│   │   └── global_headers.json          全局请求头模板（含 ${token}）
│   ├── cases/
│   │   ├── api_cases.csv                用例清单（核心驱动文件）
│   │   └── RI001_route_info/
│   │       ├── request.json             请求体参数
│   │       └── assert.json              断言规则
│   └── extract/
│       └── login_extract.json           登录响应字段提取规则（预留）
│
└── src/test/java/
    └── tests/
        └── ApiTestRunner.java           TestNG 测试类（套件执行入口）
```

---

## 一条用例的完整执行流程

```
mvn test
  │
  ├─ @BeforeSuite: LoginManager.login()
  │    ├─ 读取 config.properties → loginBaseUrl + loginPath
  │    ├─ GET 登录接口
  │    └─ 提取 $.data 中的 token → VariableContext.put("token", ...)
  │
  ├─ @DataProvider: CaseLoader.loadApiCasesFromResource("cases/api_cases.csv")
  │    ├─ 解析 CSV 每一行 → ApiCase 对象
  │    ├─ 过滤 enabled = true 的用例
  │    └─ 按 caseId 排序
  │
  └─ @Test run(ApiCase apiCase)  ←── 每条 ApiCase 执行一次
       ├─ 打印 ApiCase 信息
       ├─ apiCase.toTestCase() → TestCase（供引擎使用）
       │
       ├─ RequestBuilder.build(testCase)
       │    ├─ 读取 baseUrlKey → ConfigLoader → baseUrl
       │    ├─ VariableContext.resolve(url)   替换 ${...} 占位符
       │    ├─ GlobalHeaders.build()          加载全局头 + 注入 token
       │    └─ 读取 request.json             → requestBody（含占位符替换）
       │
       ├─ OkHttpApiHttpClient.execute(method, url, headers, body)
       │    └─ → ApiResponse(statusCode, body, elapsedMs, json)
       │
       ├─ ExtractorEngine.extractIfPresent(testCase, response)
       │    └─ 若存在 extract.json → 提取字段 → VariableContext.put(...)
       │
       ├─ AssertionEngine.assertCase(testCase, response)
       │    └─ 读取 assert.json → 执行 statusCode / jsonEquals / jsonExists 等断言
       │
       ├─ BusinessAssertRegistry.run(businessAssert, response)
       │    └─ 若 businessAssert 非空 → 调用对应策略（如 RouteInfoAssert）
       │
       └─ 打印 ExecutionResult（passed / statusCode / elapsedMs）
```

---

## CSV 用例字段说明

文件路径：`src/main/resources/cases/api_cases.csv`

| 字段 | 说明 | 示例 |
|------|------|------|
| `caseId` | 用例唯一编号，同时决定执行顺序 | `RI001` |
| `module` | 所属功能模块（仅用于日志展示） | `路线信息` |
| `apiName` → caseName | 接口名称 / 用例名称 | `路线分页查询` |
| `enabled` | 是否启用（Y/true/1 = 启用） | `Y` |
| `method` | HTTP 方法 | `POST` |
| `baseUrlKey` | config.properties 中 baseUrl 的 key | `apiBaseUrl` |
| `path` | 接口路径 | `/infoRoadSection/getInfoRoadSectionListPage` |
| `caseDir` | 用例资源目录（classpath 相对路径） | `cases/RI001_route_info` |
| `businessAssert` | 业务断言策略名，可为空 | `RouteInfoAssert` |
| `needToken` | 是否需要注入 token（新字段，默认 false） | `true` |
| `extractFile` | 自定义提取规则路径（预留，默认为空） | _(留空)_ |
| `preCaseId` | 前置用例编号（预留，默认为空） | _(留空)_ |
| `maxResponseTime` | 最大响应时间 ms（预留，默认 0） | `2000` |

---

## JSON 资源文件说明

### request.json — 请求体参数

```json
{
  "areaCode": 330000,
  "pageSize": 10,
  "pageNum": 1
}
```
支持 `${varName}` 占位符，如 `"id": "${orderId}"`。

### assert.json — 声明式断言规则

```json
{
  "statusCode": 200,
  "responseTimeLessThan": 1000,
  "jsonEquals": { "$.code": 1000, "$.msg": "操作成功" },
  "jsonExists":  ["$.data"],
  "jsonNotEmpty": ["$.data.records"],
  "jsonGreaterThan": { "$.data.total": 0 }
}
```

### extract.json — 响应字段提取规则

```json
{
  "variables": {
    "orderId": "$.data.id",
    "token":   "$.data.token"
  }
}
```
提取后写入 `VariableContext`，供后续用例通过 `${orderId}` 引用。

### global_headers.json — 全局请求头模板

```json
{
  "Content-Type": "application/json",
  "O-TOKEN": "${token}",
  "X-CLIENT": "CLIENT_WEB"
}
```
`${token}` 在每次请求前由 `GlobalHeaders.build()` 替换为实际值。

---

## 变量提取机制设计思路

```
登录响应 → ExtractorEngine → VariableContext（内存 Map）
                                      ↓
                        RequestBuilder.resolve("${token}")
                        GlobalHeaders.resolve("${token}")
```

`VariableContext` 是一个线程安全的全局 Map（`ConcurrentHashMap`），
提供 `put / get / resolve` 三个操作。
变量注入有两个来源：
1. **登录阶段**：`LoginManager.login()` 提取 token，调用 `put("token", ...)`
2. **用例执行阶段**：`ExtractorEngine` 根据 `extract.json` 提取任意字段

占位符解析规则（优先级从高到低）：
1. `extra` 临时 Map（方法级参数）
2. 全局 `STORE`（VariableContext 内存）
3. 内置：`${baseUrl}` → config, `${env:X}` → 环境变量, `${sys:X}` → JVM 属性

---

## 断言引擎设计思路

断言引擎分两层：

**第一层：声明式（AssertionEngine + assert.json）**
- 无需写代码，JSON 配置即可覆盖绝大多数接口断言场景
- 断言失败通过 AssertJ 抛出 `AssertionError`，TestNG 自动标记 FAIL

**第二层：业务逻辑（BusinessAssertStrategy 实现类）**
- 适用于跨字段联合校验、数值精度校验等复杂场景
- 新增接口的业务断言：实现接口 → 注册到 `BusinessAssertRegistry` → CSV 填写策略名
- 当前已有：`RouteInfoAssert`（校验路段里程计算一致性）

---

## 环境配置

编辑 `src/main/resources/config.properties`：
```properties
loginBaseUrl=http://your-host:port
loginPath=/backend/login?userName=xxx&password=yyy
apiBaseUrl=http://your-api-host:port
```

本地覆盖（不入 git）：新建 `src/main/resources/config.local.properties`，
格式相同，会覆盖 `config.properties` 中同名配置。

---

## 运行方式

```bash
# 运行全部用例
mvn test

# 指定环境变量覆盖 baseUrl
mvn test -DapiBaseUrl=http://staging-host:9050
```

---

## 后续扩展方向

| 方向 | 建议实现方式 |
|------|------|
| **token 自动注入** | `ApiCase.needToken=true` 时，`GlobalHeaders.build()` 检查并注入 |
| **前置用例依赖** | `ApiCase.preCaseId` 非空时，先执行前置用例并传递变量 |
| **响应时间断言** | `ApiCase.maxResponseTime > 0` 时追加到 assert.json 规则 |
| **接口用例平台化** | 替换 `CaseLoader`，改从 HTTP API 或数据库读取用例 |
| **报告增强** | `ExecutionResult` 已就绪，可写入 DB 或推送至飞书/企微 |
| **数据库校验** | 实现 `SqlAssertionHook`，替换 `NoOpSqlAssertionHook` |
| **并发执行** | TestNG `parallel="methods"` + 将 `VariableContext` 改为 ThreadLocal |
| **Mock** | 实现 `ApiHttpClient` 接口，注入 Mock 实现 |
