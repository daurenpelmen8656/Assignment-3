package snowlink.report;

import snowlink.uplink.FailureReason;

public record DeliveryResult(boolean delivered, int attempts, FailureReason failure) {

    public static DeliveryResult success(int attempts) {
        return new DeliveryResult(true, attempts, null);
    }

    public static DeliveryResult failed(int attempts, FailureReason reason) {
        return new DeliveryResult(false, attempts, reason);
    }
}
