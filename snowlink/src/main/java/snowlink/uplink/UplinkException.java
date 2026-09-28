package snowlink.uplink;

public class UplinkException extends Exception {

    private final FailureReason reason;

    public UplinkException(FailureReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public UplinkException(FailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public FailureReason reason() {
        return reason;
    }
}
