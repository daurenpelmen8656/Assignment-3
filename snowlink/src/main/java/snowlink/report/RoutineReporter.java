package snowlink.report;

import snowlink.model.Frame;
import snowlink.model.Observation;
import snowlink.model.Priority;
import snowlink.uplink.UplinkChannel;

import java.util.Locale;

public class RoutineReporter extends StationReporter {

    public RoutineReporter(UplinkChannel channel) {
        super(channel);
    }

    @Override
    public DeliveryResult report(Observation o) {
        String payload = String.format(Locale.ROOT, "R;%s;%s;depth=%.1f;temp=%.1f;wind=%.1f",
                o.stationId(), o.takenAt(), o.snowDepthCm(), o.airTempC(), o.windSpeedMs());
        return deliverOnce(new Frame(Priority.NORMAL, payload));
    }
}
