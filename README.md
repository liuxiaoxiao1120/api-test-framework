# api-test

## 项目说明

基于 **Java 17 + Maven + TestNG** 的接口自动化框架，采用 **CSV 用例清单 + 每用例独立目录**（`request.json` / `assert.json` / 可选 `extract.json` / `sql.sql`），适合命令行与 CI/CD（Jenkins、GitLab CI、GitHub Actions 等）执行。

## 架构说明

- **`src/main/java/core`**：框架核心（可被其他模块依赖），按职责分包：
  - **`core.client`**：`ApiHttpClient` 与 OkHttp 实现，对外不暴露 OkHttp 类型。
  - **`core.config`**：读取 `config.properties`。
  - **`core.context`**：`VariableContext` 变量池与 `${变量}` 解析（底层配合 `core.utils.VariableUtil`）。
  - **`core.execution`**：全局 Header 模板、`RequestBuilder` 组装请求。
  - **`core.extract`**：`ExtractorEngine` 根据 `extract.json` 写入变量池。
  - **`core.assertion`**：`AssertionEngine`、业务断言策略注册（`BusinessAssertStrategy`）。
  - **`core.loader`**：`CaseLoader` 解析 CSV 为 `TestCase`。
  - **`core.model`**：`TestCase`、`ApiResponse`、`ExecutionResult` 等模型。
  - **`core.sql`**：数据库校验扩展点（`SqlAssertionHook`，默认无操作实现）。
  - **`core.utils`**：`JsonUtil`、`LoginManager` 等工具与登录流程。
  - **`core.service` / `core.controller`**：预留包（平台化时使用，当前无实现）。
- **`src/test/java/tests`**：仅保留 **`ApiTestRunner`**，作为 TestNG 入口，调用 `core` 中的能力。
- **`src/main/resources`**：运行时资源（与核心同 JAR/classpath），包括：
  - `config.properties`：环境地址、登录路径等。
  - `headers/global_headers.json`：全局请求头模板（可通过配置 `global.headers.file` 覆盖路径）。
  - `cases/api_cases.csv`：用例清单。
  - `cases/<用例目录>/`：各用例的 `request.json`、`assert.json` 等。

## 目录结构（摘要）

```
api-test
├── pom.xml
├── testng.xml
├── README.md
├── src/main/java/core/          # 框架核心
│   ├── client/
│   ├── config/
│   ├── context/
│   ├── execution/
│   ├── extract/
│   ├── assertion/
│   ├── loader/
│   ├── model/
│   ├── sql/
│   ├── utils/
│   ├── service/                 # package-info 预留
│   └── controller/              # package-info 预留
├── src/main/resources/          # 配置、CSV、用例数据、全局 headers
│   ├── config.properties
│   ├── headers/
│   └── cases/
└── src/test/java/tests/
    └── ApiTestRunner.java       # TestNG 入口
```

## 如何运行

需已安装 **JDK 17** 与 **Maven**，在项目根目录执行：

```bash
mvn clean test
```

## 如何新增用例

1. 在 `src/main/resources/cases/` 下新建用例目录，例如 `cases/MY_CASE_001/`，放入 `request.json`、`assert.json`（可选 `extract.json`、`sql.sql`）。
2. 在 `src/main/resources/cases/api_cases.csv` 中追加一行，填写 `caseId`、`method`、`baseUrlKey`、`path`、`caseDir`（指向上述目录的 classpath 相对路径）等字段；`businessAssert` 列可填已注册的策略名（如 `RouteInfoAssert`），留空则只做声明式断言。
