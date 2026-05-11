package core.loader;

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
 * Reads the case manifest CSV into {@link TestCase} rows.
 */
public final class CaseLoader {

    private CaseLoader() {
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
