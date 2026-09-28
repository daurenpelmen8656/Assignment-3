package snowlink;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import snowlink.model.Frame;
import snowlink.model.Priority;
import snowlink.satellite.SatelliteUplinkAdapter;
import snowlink.uplink.FailureReason;
import snowlink.uplink.UplinkException;
import vendor.satmodem.SatelliteModem;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SatelliteUplinkAdapterTest {

    private SatelliteModem modem;
    private SatelliteUplinkAdapter adapter;

    @BeforeEach
    void setUp() {
        modem = mock(SatelliteModem.class);
        when(modem.powerUp()).thenReturn(SatelliteModem.STATUS_OK);
        when(modem.sendBurst(any(), anyInt(), anyString())).thenReturn(SatelliteModem.STATUS_OK);
        adapter = new SatelliteUplinkAdapter(modem, "ops-7");
    }

    private Frame frame(Priority priority) {
        return new Frame(priority, "ALERT;st-1;hazard=4");
    }

    private void assertReason(int status, FailureReason expected) {
        when(modem.sendBurst(any(), anyInt(), anyString())).thenReturn(status);

        UplinkException e = assertThrows(UplinkException.class, () -> adapter.transmit(frame(Priority.NORMAL)));

        assertEquals(expected, e.reason());
    }

    @Test
    void framePayloadAndRecipientGoToModem() throws Exception {
        adapter.transmit(frame(Priority.CRITICAL));

        ArgumentCaptor<byte[]> payload = ArgumentCaptor.forClass(byte[].class);
        verify(modem).sendBurst(payload.capture(), anyInt(), eq("ops-7"));
        assertArrayEquals("ALERT;st-1;hazard=4".getBytes(StandardCharsets.UTF_8), payload.getValue());
    }

    @Test
    void prioritiesAreMappedToVendorCodes() throws Exception {
        adapter.transmit(frame(Priority.CRITICAL));
        adapter.transmit(frame(Priority.NORMAL));
        adapter.transmit(frame(Priority.LOW));

        verify(modem).sendBurst(any(), eq(SatelliteModem.PRIORITY_EMERGENCY), anyString());
        verify(modem).sendBurst(any(), eq(SatelliteModem.PRIORITY_STANDARD), anyString());
        verify(modem).sendBurst(any(), eq(SatelliteModem.PRIORITY_BULK), anyString());
    }

    @Test
    void modemIsPoweredUpOnlyOnce() throws Exception {
        adapter.transmit(frame(Priority.NORMAL));
        adapter.transmit(frame(Priority.NORMAL));

        verify(modem, times(1)).powerUp();
    }

    @Test
    void noSignalIsUnreachable() {
        assertReason(SatelliteModem.STATUS_NO_SIGNAL, FailureReason.UNREACHABLE);
    }

    @Test
    void queueFullIsOverloaded() {
        assertReason(SatelliteModem.STATUS_QUEUE_FULL, FailureReason.OVERLOADED);
    }

    @Test
    void invalidRequestIsRejected() {
        assertReason(SatelliteModem.STATUS_INVALID_REQUEST, FailureReason.REJECTED);
    }

    @Test
    void hardwareFaultAndUnknownCodesAreFaults() {
        assertReason(SatelliteModem.STATUS_HARDWARE_FAULT, FailureReason.FAULT);
        assertReason(999, FailureReason.FAULT);
    }

    @Test
    void modemThatCannotPowerUpIsUnreachable() {
        when(modem.powerUp()).thenReturn(SatelliteModem.STATUS_HARDWARE_FAULT);

        UplinkException e = assertThrows(UplinkException.class, () -> adapter.transmit(frame(Priority.NORMAL)));

        assertEquals(FailureReason.UNREACHABLE, e.reason());
        verify(modem, never()).sendBurst(any(), anyInt(), anyString());
    }

    @Test
    void modemThatFellAsleepIsWokenUpAndFrameIsSentAgain() {
        when(modem.sendBurst(any(), anyInt(), anyString()))
                .thenReturn(SatelliteModem.STATUS_NOT_POWERED, SatelliteModem.STATUS_OK);

        assertDoesNotThrow(() -> adapter.transmit(frame(Priority.NORMAL)));

        verify(modem, times(2)).powerUp();
        verify(modem, times(2)).sendBurst(any(), anyInt(), anyString());
    }

    @Test
    void modemThatStaysAsleepIsUnreachable() {
        assertReason(SatelliteModem.STATUS_NOT_POWERED, FailureReason.UNREACHABLE);
    }

    @Test
    void runtimeExceptionFromVendorIsWrapped() {
        IllegalStateException boom = new IllegalStateException("driver crashed");
        when(modem.sendBurst(any(), anyInt(), anyString())).thenThrow(boom);

        UplinkException e = assertThrows(UplinkException.class, () -> adapter.transmit(frame(Priority.NORMAL)));

        assertEquals(FailureReason.FAULT, e.reason());
        assertSame(boom, e.getCause());
    }
}
