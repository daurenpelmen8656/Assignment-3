package snowlink.report;

import snowlink.model.Frame;
import snowlink.model.Observation;
import snowlink.model.Priority;
import snowlink.uplink.UplinkChannel;

import java.util.Locale;

public class DiagnosticsReporter extends StationReporter {

    public DiagnosticsReporter(UplinkChannel channel) {
        super(channel);
    }

    @Override
    public DeliveryResult report(Observation o) {
        String payload = String.format(Locale.ROOT, "DIAG;%s;%s;battery=%d",
                o.stationId(), o.takenAt(), o.batteryPercent());
        return deliverOnce(new Frame(Priority.LOW, payload));
    }
}
