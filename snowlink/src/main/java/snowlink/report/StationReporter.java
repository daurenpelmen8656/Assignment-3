package snowlink.report;

import snowlink.model.Frame;
import snowlink.model.Observation;
import snowlink.uplink.FailureReason;
import snowlink.uplink.UplinkChannel;
import snowlink.uplink.UplinkException;

import java.time.Duration;

public abstract class StationReporter {

    private final UplinkChannel channel;

    protected StationReporter(UplinkChannel channel) {
        this.channel = channel;
    }

    public abstract DeliveryResult report(Observation observation);

    protected DeliveryResult deliverOnce(Frame frame) {
        return deliver(frame, 1, Duration.ZERO);
    }

    protected DeliveryResult deliver(Frame frame, int maxAttempts, Duration backoff) {
        int attempt = 0;
        FailureReason last = null;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                channel.transmit(frame);
                return DeliveryResult.success(attempt);
            } catch (UplinkException e) {
                last = e.reason();
                if (!last.retryable() || attempt == maxAttempts) {
                    break;
                }
                sleep(backoff);
            }
        }
        return DeliveryResult.failed(attempt, last);
    }

    private void sleep(Duration backoff) {
        try {
            Thread.sleep(backoff.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
