package snowlink;

import org.junit.jupiter.api.Test;
import snowlink.model.Frame;
import snowlink.model.Observation;
import snowlink.model.Priority;
import snowlink.report.AvalancheAlertReporter;
import snowlink.report.DeliveryResult;
import snowlink.report.DiagnosticsReporter;
import snowlink.report.RoutineReporter;
import snowlink.report.StationReporter;
import snowlink.routing.ChannelRegistry;
import snowlink.uplink.UplinkChannel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static snowlink.TestData.OBSERVATION;

class OpenClosedTest {

    private static class MemoryChannel implements UplinkChannel {
        List<Frame> frames = new ArrayList<>();

        @Override
        public void transmit(Frame frame) {
            frames.add(frame);
        }
    }

    private static class HourlyReporter extends StationReporter {
        HourlyReporter(UplinkChannel channel) {
            super(channel);
        }

        @Override
        public DeliveryResult report(Observation o) {
            return deliver(new Frame(Priority.LOW, "HOURLY;" + o.stationId()), 2, Duration.ZERO);
        }
    }

    @Test
    void newChannelWorksWithAllReporters() {
        MemoryChannel channel = new MemoryChannel();

        new RoutineReporter(channel).report(OBSERVATION);
        new AvalancheAlertReporter(channel).report(OBSERVATION);
        new DiagnosticsReporter(channel).report(OBSERVATION);

        assertEquals(3, channel.frames.size());
    }

    @Test
    void newChannelCanBeRegistered() {
        MemoryChannel channel = new MemoryChannel();
        ChannelRegistry registry = new ChannelRegistry().register("memory", route -> channel);

        new RoutineReporter(registry.open("memory://any")).report(OBSERVATION);

        assertEquals(1, channel.frames.size());
    }

    @Test
    void newReporterWorksWithExistingChannel() {
        MemoryChannel channel = new MemoryChannel();

        DeliveryResult result = new HourlyReporter(channel).report(OBSERVATION);

        assertTrue(result.delivered());
        assertEquals("HOURLY;st-1", channel.frames.get(0).payload());
    }
}
