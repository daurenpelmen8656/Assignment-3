package snowlink;

import snowlink.model.Observation;

import java.time.Instant;

final class TestData {

    static final Observation OBSERVATION =
            new Observation("st-1", Instant.parse("2026-01-15T08:00:00Z"), 120.0, -5.5, 8.0, 3, 64);

    private TestData() {
    }
}
