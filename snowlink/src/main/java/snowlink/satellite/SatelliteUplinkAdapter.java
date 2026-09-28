package snowlink.satellite;

import snowlink.model.Frame;
import snowlink.model.Priority;
import snowlink.uplink.FailureReason;
import snowlink.uplink.UplinkChannel;
import snowlink.uplink.UplinkException;
import vendor.satmodem.SatelliteModem;

import java.nio.charset.StandardCharsets;

public final class SatelliteUplinkAdapter implements UplinkChannel {

    private final SatelliteModem modem;
    private final String recipientId;
    private boolean poweredUp = false;

    public SatelliteUplinkAdapter(SatelliteModem modem, String recipientId) {
        this.modem = modem;
        this.recipientId = recipientId;
    }

    @Override
    public synchronized void transmit(Frame frame) throws UplinkException {
        byte[] payload = frame.payload().getBytes(StandardCharsets.UTF_8);
        int priorityCode = toVendorPriority(frame.priority());
        try {
            ensurePoweredUp();
            int status = modem.sendBurst(payload, priorityCode, recipientId);
            if (status == SatelliteModem.STATUS_NOT_POWERED) {
                poweredUp = false;
                ensurePoweredUp();
                status = modem.sendBurst(payload, priorityCode, recipientId);
            }
            if (status != SatelliteModem.STATUS_OK) {
                throw translate(status);
            }
        } catch (RuntimeException e) {
            throw new UplinkException(FailureReason.FAULT, "Satellite link failed unexpectedly", e);
        }
    }

    private void ensurePoweredUp() throws UplinkException {
        if (poweredUp) {
            return;
        }
        if (modem.powerUp() != SatelliteModem.STATUS_OK) {
            throw new UplinkException(FailureReason.UNREACHABLE, "Satellite link could not be started");
        }
        poweredUp = true;
    }

    private static int toVendorPriority(Priority priority) {
        if (priority == Priority.CRITICAL) {
            return SatelliteModem.PRIORITY_EMERGENCY;
        }
        if (priority == Priority.NORMAL) {
            return SatelliteModem.PRIORITY_STANDARD;
        }
        return SatelliteModem.PRIORITY_BULK;
    }

    private static UplinkException translate(int status) {
        if (status == SatelliteModem.STATUS_NO_SIGNAL || status == SatelliteModem.STATUS_NOT_POWERED) {
            return new UplinkException(FailureReason.UNREACHABLE, "Satellite link unavailable");
        }
        if (status == SatelliteModem.STATUS_QUEUE_FULL) {
            return new UplinkException(FailureReason.OVERLOADED, "Satellite link is saturated, try again later");
        }
        if (status == SatelliteModem.STATUS_INVALID_REQUEST) {
            return new UplinkException(FailureReason.REJECTED, "Satellite link refused the frame");
        }
        return new UplinkException(FailureReason.FAULT, "Satellite link reported a problem");
    }
}
