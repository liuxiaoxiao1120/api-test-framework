package core.assertion;

import com.fasterxml.jackson.databind.JsonNode;
import core.model.ApiResponse;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RouteInfoAssert {
    private RouteInfoAssert() {
    }

    public static void assertRouteInfoBusiness(ApiResponse resp) {
        JsonNode root = resp.getJson();
        Assertions.assertThat(root)
                .as("response json")
                .isNotNull();

        JsonNode records = root.path("data").path("records");
        Assertions.assertThat(records.isArray())
                .as("$.data.records is array")
                .isTrue();

        for (int i = 0; i < records.size(); i++) {
            JsonNode r = records.get(i);
            Assertions.assertThat(r.path("roadNo").asText())
                    .as("records[%s].roadNo", i)
                    .isNotBlank();
            Assertions.assertThat(r.path("roadName").asText())
                    .as("records[%s].roadName", i)
                    .isNotBlank();
            Assertions.assertThat(r.path("roadSectionNo").asText())
                    .as("records[%s].roadSectionNo", i)
                    .isNotBlank();
            Assertions.assertThat(r.path("startName").asText())
                    .as("records[%s].startName", i)
                    .isNotBlank();
            Assertions.assertThat(r.path("endName").asText())
                    .as("records[%s].endName", i)
                    .isNotBlank();

            double mileage = r.path("mileage").asDouble();
            double startMilestone = r.path("startMilestone").asDouble();
            double endMilestone = r.path("endMilestone").asDouble();

            Assertions.assertThat(mileage)
                    .as("records[%s].mileage", i)
                    .isGreaterThanOrEqualTo(0d);

            Assertions.assertThat(endMilestone)
                    .as("records[%s].endMilestone >= startMilestone", i)
                    .isGreaterThanOrEqualTo(startMilestone);

            double diff = round(endMilestone - startMilestone, 6);
            double mileageRounded = round(mileage, 6);
            Assertions.assertThat(mileageRounded)
                    .as("records[%s].mileage approx endMilestone-startMilestone", i)
                    .isCloseTo(diff, Assertions.within(0.001d));
        }
    }

    private static double round(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
