package tests;

import assertion.BusinessAssertDispatcher;
import assertion.CommonAssert;
import client.HttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import config.ConfigLoader;
import model.ApiCase;
import model.ApiResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.CsvUtil;
import utils.JsonUtil;
import utils.VariableUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiTestRunner {

    private final HttpClientUtil http = new HttpClientUtil();

    @DataProvider(name = "apiCases")
    public Object[][] apiCases() {
        List<ApiCase> all = CsvUtil.readApiCasesFromResource("cases/api_cases.csv");
        List<ApiCase> enabled = new ArrayList<>();
        for (ApiCase c : all) {
            if (c.isEnabled()) {
                enabled.add(c);
            }
        }
        enabled.sort(Comparator.comparing(ApiCase::getCaseId, Comparator.nullsLast(String::compareToIgnoreCase)));
        Object[][] data = new Object[enabled.size()][1];
        for (int i = 0; i < enabled.size(); i++) {
            data[i][0] = enabled.get(i);
        }
        return data;
    }

    @Test(dataProvider = "apiCases")
    public void run(ApiCase c) throws Exception {
        String baseUrlKey = c.getBaseUrlKey();
        if (baseUrlKey == null || baseUrlKey.isBlank()) {
            baseUrlKey = "apiBaseUrl";
        }
        String baseUrl = ConfigLoader.getRequiredProperty(baseUrlKey.trim());
        baseUrl = VariableUtil.resolve(baseUrl);

        String path = c.getPath() == null ? "" : c.getPath().trim();
        String normalizedPath = path.startsWith("/") ? path : ("/" + path);
        String url = baseUrl.replaceAll("/+$", "") + normalizedPath;

        Map<String, String> headers = Map.of();
        if (c.getHeadersFile() != null && !c.getHeadersFile().isBlank()) {
            Map<String, String> raw = JsonUtil.readStringMapFromResource(c.getHeadersFile().trim());
            Map<String, String> resolved = new HashMap<>();
            for (Map.Entry<String, String> e : raw.entrySet()) {
                String value = e.getValue();
                value = value == null ? null : VariableUtil.resolve(value);
                resolved.put(e.getKey(), value);
            }
            headers = resolved;
        }

        String requestBody = null;
        if (c.getRequestFile() != null && !c.getRequestFile().isBlank()) {
            requestBody = JsonUtil.readTextFromResource(c.getRequestFile().trim());
            requestBody = VariableUtil.resolve(requestBody);
        }

        System.out.println("========== API TEST ==========");
        System.out.println("caseId: " + c.getCaseId());
        System.out.println("module: " + c.getModule());
        System.out.println("apiName: " + c.getApiName());
        System.out.println("method: " + c.getMethod());
        System.out.println("url: " + url);
        System.out.println("requestBody: " + (requestBody == null ? "" : requestBody));

        HttpClientUtil.HttpResponse raw = http.request(c.getMethod(), url, headers, requestBody);

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

        System.out.println("responseBody: " + resp.getBody());
        System.out.println("elapsedMs: " + resp.getElapsedMs());
        System.out.println("==============================");

        // basic sanity: method/path should exist
        assertThat(c.getMethod()).as("method").isNotBlank();
        assertThat(c.getPath()).as("path").isNotBlank();

        if (c.getExtractFile() != null && !c.getExtractFile().isBlank() && resp.getJson() != null) {
            Map<String, String> extractMap =
                    JsonUtil.readFromResource(c.getExtractFile().trim(), new TypeReference<Map<String, String>>() {
                    });
            for (Map.Entry<String, String> e : extractMap.entrySet()) {
                String varName = e.getKey();
                String jsonPath = e.getValue();
                JsonNode node = JsonUtil.getByJsonPath(resp.getJson(), jsonPath);
                if (node != null && !node.isMissingNode() && !node.isNull()) {
                    String value = node.isTextual() ? node.asText() : node.toString();
                    if (value != null && !value.isBlank()) {
                        VariableUtil.set(varName, value);
                        System.out.println("EXTRACT -> " + varName + " = " + value);
                    }
                }
            }
        }

        CommonAssert.assertByRuleFile(c, resp);
        BusinessAssertDispatcher.dispatch(c, resp);
    }
}

