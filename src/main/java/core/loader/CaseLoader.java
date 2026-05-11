package core.loader;

import core.model.ApiCase;
import core.model.TestCase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the case manifest CSV into {@link TestCase} or {@link ApiCase} rows.
 */
public final class CaseLoader {

    private CaseLoader() {
    }

    /**
     * 读取 CSV 并转换为 {@link ApiCase} 对象列表。
     * <p>对 CSV 中不存在的新字段（needToken、extractFile、preCaseId、maxResponseTime）
     * 自动给默认值，不会报错。</p>
     */
    public static List<ApiCase> loadApiCasesFromResource(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalized);
        if (in == null) {
            throw new IllegalArgumentException("CSV resource not found: " + resourcePath);
        }

        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<ApiCase> list = new ArrayList<>();
            for (CSVRecord r : parser) {
                ApiCase c = new ApiCase();
                c.setCaseId(readOptionalColumn(r, "caseId"));
                c.setCaseName(readOptionalColumn(r, "apiName"));
                c.setModule(readOptionalColumn(r, "module"));
                c.setMethod(readOptionalColumn(r, "method"));
                c.setPath(readOptionalColumn(r, "path"));
                c.setBaseUrlKey(readOptionalColumn(r, "baseUrlKey"));

                String caseDir = readOptionalColumn(r, "caseDir");
                c.setCaseDir(caseDir);
                c.setRequestFile(joinPath(caseDir, "request.json"));
                c.setAssertFile(joinPath(caseDir, "assert.json"));

                c.setBusinessAssert(readOptionalColumn(r, "businessAssert"));

                // enabled: Y/N/true/false → boolean，默认 true
                String enabledStr = readOptionalColumn(r, "enabled");
                c.setEnabled(parseBooleanEnabled(enabledStr));

                // 以下为新增字段，CSV 中可能不存在，给默认值
                String needTokenStr = readOptionalColumn(r, "needToken");
                c.setNeedToken(parseBooleanFlag(needTokenStr));

                c.setExtractFile(readOptionalColumn(r, "extractFile"));
                c.setPreCaseId(readOptionalColumn(r, "preCaseId"));

                String maxRespStr = readOptionalColumn(r, "maxResponseTime");
                c.setMaxResponseTime(maxRespStr.isEmpty() ? 0L : parseLongSafe(maxRespStr));

                list.add(c);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV: " + resourcePath, e);
        }
    }

    private static String joinPath(String dir, String fileName) {
        if (dir == null || dir.isBlank()) {
            return fileName;
        }
        String d = dir.trim();
        return d.endsWith("/") ? d + fileName : d + "/" + fileName;
    }

    /**
     * enabled 列：Y/yes/true/1 → true，空值默认 true，其他 → false。
     */
    private static boolean parseBooleanEnabled(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String v = value.trim();
        return v.equalsIgnoreCase("Y")
                || v.equalsIgnoreCase("yes")
                || v.equalsIgnoreCase("true")
                || v.equals("1");
    }

    /**
     * 普通布尔标志：true/yes/1 → true，空值默认 false。
     */
    private static boolean parseBooleanFlag(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String v = value.trim();
        return v.equalsIgnoreCase("true")
                || v.equalsIgnoreCase("yes")
                || v.equals("1");
    }

    private static long parseLongSafe(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static List<TestCase> loadFromResource(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalized);
        if (in == null) {
            throw new IllegalArgumentException("CSV resource not found: " + resourcePath);
        }

        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<TestCase> list = new ArrayList<>();
            for (CSVRecord r : parser) {
                TestCase c = new TestCase();
                c.setCaseId(r.get("caseId"));
                c.setModule(r.get("module"));
                c.setApiName(r.get("apiName"));
                c.setEnabled(r.get("enabled"));
                c.setMethod(r.get("method"));
                c.setBaseUrlKey(r.get("baseUrlKey"));
                c.setPath(r.get("path"));
                c.setCaseDir(r.get("caseDir"));
                c.setBusinessAssert(readOptionalColumn(r, "businessAssert"));
                list.add(c);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV: " + resourcePath, e);
        }
    }

    private static String readOptionalColumn(CSVRecord r, String column) {
        if (!r.isMapped(column)) {
            return "";
        }
        try {
            String v = r.get(column);
            return v == null ? "" : v.trim();
        } catch (Exception e) {
            return "";
        }
    }
}
