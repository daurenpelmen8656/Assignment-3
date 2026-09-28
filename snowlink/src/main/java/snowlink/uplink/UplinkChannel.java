package snowlink.uplink;

import snowlink.model.Frame;

public interface UplinkChannel {

    void transmit(Frame frame) throws UplinkException;
}
