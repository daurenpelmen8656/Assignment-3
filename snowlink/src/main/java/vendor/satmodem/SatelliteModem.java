package vendor.satmodem;

import java.util.ArrayDeque;
import java.util.Deque;

public class SatelliteModem {

    public static final int STATUS_OK = 0;
    public static final int STATUS_NO_SIGNAL = 12;
    public static final int STATUS_QUEUE_FULL = 17;
    public static final int STATUS_INVALID_REQUEST = 21;
    public static final int STATUS_NOT_POWERED = 30;
    public static final int STATUS_HARDWARE_FAULT = 40;

    public static final int MAX_PAYLOAD_BYTES = 340;

    public static final int PRIORITY_EMERGENCY = 0;
    public static final int PRIORITY_STANDARD = 2;
    public static final int PRIORITY_BULK = 3;

    private final int queueCapacity;
    private final Deque<byte[]> outbox = new ArrayDeque<>();
    private boolean powered;
    private boolean faulty;
    private int signalBars = 3;

    public SatelliteModem(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int powerUp() {
        if (faulty) {
            return STATUS_HARDWARE_FAULT;
        }
        powered = true;
        return STATUS_OK;
    }

    public void powerDown() {
        powered = false;
    }

    public int sendBurst(byte[] payload, int priorityCode, String recipientId) {
        if (!powered) {
            return STATUS_NOT_POWERED;
        }
        if (payload == null || payload.length == 0 || payload.length > MAX_PAYLOAD_BYTES
                || priorityCode < PRIORITY_EMERGENCY || priorityCode > PRIORITY_BULK
                || recipientId == null || recipientId.isBlank()) {
            return STATUS_INVALID_REQUEST;
        }
        if (signalBars == 0) {
            return STATUS_NO_SIGNAL;
        }
        if (outbox.size() >= queueCapacity) {
            return STATUS_QUEUE_FULL;
        }
        outbox.add(payload.clone());
        return STATUS_OK;
    }

    public int pendingBursts() {
        return outbox.size();
    }

        public int flushOnNextPass() {
        int sent = outbox.size();
        outbox.clear();
        return sent;
    }

        public void setSignalBars(int bars) {
        this.signalBars = bars;
    }

        public void simulateHardwareFault(boolean faulty) {
        this.faulty = faulty;
    }
}
