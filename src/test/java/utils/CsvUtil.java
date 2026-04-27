package utils;

import model.ApiCase;
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

public final class CsvUtil {
    private CsvUtil() {
    }

    public static List<ApiCase> readApiCasesFromResource(String resourcePath) {
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
                c.setCaseId(r.get("caseId"));
                c.setModule(r.get("module"));
                c.setApiName(r.get("apiName"));
                c.setEnabled(r.get("enabled"));
                c.setMethod(r.get("method"));
                c.setBaseUrlKey(r.get("baseUrlKey"));
                c.setPath(r.get("path"));
                c.setHeadersFile(r.get("headersFile"));
                c.setRequestFile(r.get("requestFile"));
                c.setAssertFile(r.get("assertFile"));
                c.setBusinessAssert(r.get("businessAssert"));
                c.setExtractFile(r.get("extractFile"));
                c.setMaxResponseTime(Long.parseLong(r.get("maxResponseTime")));
                list.add(c);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV: " + resourcePath, e);
        }
    }
}

