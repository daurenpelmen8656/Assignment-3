package snowlink.report;

import snowlink.model.Frame;
import snowlink.model.Observation;
import snowlink.model.Priority;
import snowlink.uplink.UplinkChannel;

import java.time.Duration;
import java.util.Locale;

public class AvalancheAlertReporter extends StationReporter {

    private final int maxAttempts;
    private final Duration backoff;

    public AvalancheAlertReporter(UplinkChannel channel) {
        this(channel, 3, Duration.ofMillis(200));
    }

    public AvalancheAlertReporter(UplinkChannel channel, int maxAttempts, Duration backoff) {
        super(channel);
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
    }

    @Override
    public DeliveryResult report(Observation o) {
        String payload = String.format(Locale.ROOT, "ALERT;%s;%s;hazard=%d;depth=%.1f;wind=%.1f",
                o.stationId(), o.takenAt(), o.hazardLevel(), o.snowDepthCm(), o.windSpeedMs());
        return deliver(new Frame(Priority.CRITICAL, payload), maxAttempts, backoff);
    }
}
