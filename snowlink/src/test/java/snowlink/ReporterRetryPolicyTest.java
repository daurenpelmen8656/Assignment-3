package snowlink;

import org.junit.jupiter.api.Test;
import snowlink.model.Frame;
import snowlink.report.AvalancheAlertReporter;
import snowlink.report.DeliveryResult;
import snowlink.report.RoutineReporter;
import snowlink.uplink.FailureReason;
import snowlink.uplink.UplinkChannel;
import snowlink.uplink.UplinkException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static snowlink.TestData.OBSERVATION;

class ReporterRetryPolicyTest {

    private final UplinkChannel channel = mock(UplinkChannel.class);

    @Test
    void alertRetriesWhenOverloadedAndThenSucceeds() throws Exception {
        doThrow(new UplinkException(FailureReason.OVERLOADED, "busy")).doNothing().when(channel).transmit(any(Frame.class));

        DeliveryResult result = new AvalancheAlertReporter(channel, 3, Duration.ZERO).report(OBSERVATION);

        assertTrue(result.delivered());
        assertEquals(2, result.attempts());
        verify(channel, times(2)).transmit(any(Frame.class));
    }

    @Test
    void alertGivesUpAfterMaxAttempts() throws Exception {
        doThrow(new UplinkException(FailureReason.OVERLOADED, "busy")).when(channel).transmit(any(Frame.class));

        DeliveryResult result = new AvalancheAlertReporter(channel, 3, Duration.ZERO).report(OBSERVATION);

        assertFalse(result.delivered());
        assertEquals(3, result.attempts());
        assertEquals(FailureReason.OVERLOADED, result.failure());
    }

    @Test
    void alertDoesNotRetryRejected() throws Exception {
        doThrow(new UplinkException(FailureReason.REJECTED, "no")).when(channel).transmit(any(Frame.class));

        DeliveryResult result = new AvalancheAlertReporter(channel, 3, Duration.ZERO).report(OBSERVATION);

        assertFalse(result.delivered());
        assertEquals(1, result.attempts());
        assertEquals(FailureReason.REJECTED, result.failure());
        verify(channel, times(1)).transmit(any(Frame.class));
    }

    @Test
    void routineReporterNeverRetries() throws Exception {
        doThrow(new UplinkException(FailureReason.OVERLOADED, "busy")).when(channel).transmit(any(Frame.class));

        DeliveryResult result = new RoutineReporter(channel).report(OBSERVATION);

        assertFalse(result.delivered());
        assertEquals(1, result.attempts());
        verify(channel, times(1)).transmit(any(Frame.class));
    }
}
