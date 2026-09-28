package snowlink;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import snowlink.model.Frame;
import snowlink.model.Priority;
import snowlink.report.AvalancheAlertReporter;
import snowlink.report.DeliveryResult;
import snowlink.report.DiagnosticsReporter;
import snowlink.report.RoutineReporter;
import snowlink.uplink.UplinkChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static snowlink.TestData.OBSERVATION;

class ReporterDelegationTest {

    private final UplinkChannel channel = mock(UplinkChannel.class);

    private Frame sentFrame() throws Exception {
        ArgumentCaptor<Frame> captor = ArgumentCaptor.forClass(Frame.class);
        verify(channel).transmit(captor.capture());
        return captor.getValue();
    }

    @Test
    void routineReporterSendsNormalFrame() throws Exception {
        DeliveryResult result = new RoutineReporter(channel).report(OBSERVATION);

        Frame frame = sentFrame();
        assertEquals(Priority.NORMAL, frame.priority());
        assertEquals("R;st-1;2026-01-15T08:00:00Z;depth=120.0;temp=-5.5;wind=8.0", frame.payload());
        assertTrue(result.delivered());
    }

    @Test
    void alertReporterSendsCriticalFrame() throws Exception {
        DeliveryResult result = new AvalancheAlertReporter(channel).report(OBSERVATION);

        Frame frame = sentFrame();
        assertEquals(Priority.CRITICAL, frame.priority());
        assertEquals("ALERT;st-1;2026-01-15T08:00:00Z;hazard=3;depth=120.0;wind=8.0", frame.payload());
        assertTrue(result.delivered());
    }

    @Test
    void diagnosticsReporterSendsLowFrame() throws Exception {
        DeliveryResult result = new DiagnosticsReporter(channel).report(OBSERVATION);

        Frame frame = sentFrame();
        assertEquals(Priority.LOW, frame.priority());
        assertEquals("DIAG;st-1;2026-01-15T08:00:00Z;battery=64", frame.payload());
        assertTrue(result.delivered());
    }
}
