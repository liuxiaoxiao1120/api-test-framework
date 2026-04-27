# api-test

## 项目说明

这是一个基于 **Java 17 + Maven + TestNG** 的接口自动化测试框架，用于测试“农村公路科学决策系统”的接口。

当前内置第一个示例用例：**路线信息分页查询接口**（CSV 数据驱动）。

## 目录结构

```
api-test
├── pom.xml
├── testng.xml
├── README.md
├── src/test/java
│   ├── client
│   │   └── HttpClientUtil.java
│   ├── config
│   │   └── ConfigLoader.java
│   ├── model
│   │   └── ApiCase.java
│   ├── tests
│   │   └── RouteInfoApiTest.java
│   └── utils
│       ├── CsvUtil.java
│       └── JsonUtil.java
└── src/test/resources
    ├── config.properties
    ├── cases
    │   └── route_info_cases.csv
    └── request
        └── route_info
            └── query_page_1.json
```

## 如何运行

确保已安装 JDK 17 与 Maven，然后在项目根目录执行：

```bash
mvn clean test
```

## 如何新增接口用例

1. 在 `src/test/resources/request/<模块名>/` 下新增请求 JSON（例如 `request/route_info/query_page_2.json`）。
2. 在 `src/test/resources/cases/` 下对应 CSV 中追加一行用例数据，字段含义如下：
   - `caseId`：用例编号
   - `caseName`：用例名称
   - `method`：HTTP 方法（目前示例用例使用 POST）
   - `path`：接口路径（不含 baseUrl）
   - `requestFile`：请求体 JSON 文件路径（相对 `src/test/resources`）
   - `expectCode/expectMsg`：业务码与消息断言
   - `expectPageNum/expectPageSize`：分页断言
   - `maxResponseTime`：最大响应时间（毫秒）
3. 参照 `tests.RouteInfoApiTest` 新增测试类，并在 `testng.xml` 中添加 `<class name="..."/>`。

