package tests;

import assertion.AssertionEngine;
import assertion.BusinessAssertRegistry;
import client.ApiHttpClient;
import client.OkHttpApiHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import execution.RequestBuilder;
import extract.ExtractorEngine;
import loader.CaseLoader;
import model.ApiResponse;
import model.ExecutionResult;
import model.TestCase;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import sql.NoOpSqlAssertionHook;
import sql.SqlAssertionHook;
import utils.JsonUtil;
import utils.LoginManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiTestRunner {

    private final ApiHttpClient http = new OkHttpApiHttpClient();
    private final SqlAssertionHook sqlHook = new NoOpSqlAssertionHook();

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws Exception {
        LoginManager.login();
    }

    @DataProvider(name = "apiCases")
    public Object[][] apiCases() {
        List<TestCase> all = CaseLoader.loadFromResource("cases/api_cases.csv");
        List<TestCase> enabled = new ArrayList<>();
        for (TestCase c : all) {
            if (c.isEnabled()) {
                enabled.add(c);
            }
        }
        enabled.sort(Comparator.comparing(TestCase::getCaseId, Comparator.nullsLast(String::compareToIgnoreCase)));
        Object[][] data = new Object[enabled.size()][1];
        for (int i = 0; i < enabled.size(); i++) {
            data[i][0] = enabled.get(i);
        }
        return data;
    }

    @Test(dataProvider = "apiCases")
    public void run(TestCase c) throws Exception {
        ExecutionResult result = new ExecutionResult();
        result.setCaseId(c.getCaseId());

        assertThat(c.getMethod()).as("method").isNotBlank();
        assertThat(c.getPath()).as("path").isNotBlank();
        assertThat(c.getCaseDir()).as("caseDir").isNotBlank();

        RequestBuilder.BuiltRequest built = RequestBuilder.build(c);

        System.out.println("========== API TEST ==========");
        System.out.println("caseId: " + c.getCaseId());
        System.out.println("module: " + c.getModule());
        System.out.println("apiName: " + c.getApiName());
        System.out.println("method: " + c.getMethod());
        System.out.println("url: " + built.url());
        System.out.println("requestBody: " + (built.body() == null ? "" : built.body()));

        ApiHttpClient.HttpResult raw = http.execute(c.getMethod(), built.url(), built.headers(), built.body());

        JsonNode json = null;
        String body = raw.body() == null ? "" : raw.body();
        if (!body.isBlank()) {
            try {
                json = JsonUtil.mapper().readTree(body);
            } catch (Exception ignore) {
                json = null;
            }
        }

        ApiResponse resp = new ApiResponse(raw.statusCode(), body, raw.elapsedMs(), json);

        result.setStatusCode(resp.getStatusCode());
        result.setElapsedMs(resp.getElapsedMs());
        result.setResponseBody(resp.getBody());

        System.out.println("responseBody: " + resp.getBody());
        System.out.println("elapsedMs: " + resp.getElapsedMs());
        System.out.println("==============================");

        try {
            ExtractorEngine.extractIfPresent(c, resp);
            AssertionEngine.assertCase(c, resp);
            BusinessAssertRegistry.run(c.getBusinessAssert(), resp);
            sqlHook.runIfPresent(c);
            result.setPassed(true);
        } catch (AssertionError e) {
            result.setPassed(false);
            result.setFailureReason(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            result.setPassed(false);
            result.setFailureReason(String.valueOf(e.getMessage()));
            throw e;
        } finally {
            printExecutionSummary(result);
        }
    }

    private static void printExecutionSummary(ExecutionResult result) {
        System.out.println("----- execution result -----");
        System.out.println("caseId: " + result.getCaseId() + ", passed: " + result.isPassed()
                + ", status: " + result.getStatusCode() + ", elapsedMs: " + result.getElapsedMs());
        if (result.getFailureReason() != null) {
            System.out.println("failure: " + result.getFailureReason());
        }
        System.out.println("----------------------------");
    }
}
