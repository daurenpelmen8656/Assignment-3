package snowlink.uplink;

public enum FailureReason {

    UNREACHABLE(false),
    OVERLOADED(true),
    REJECTED(false),
    FAULT(false);

    private final boolean retryable;

    FailureReason(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean retryable() {
        return retryable;
    }
}
