package snowlink.model;

import java.time.Instant;

public record Observation(String stationId,
                          Instant takenAt,
                          double snowDepthCm,
                          double airTempC,
                          double windSpeedMs,
                          int hazardLevel,
                          int batteryPercent) {
}
