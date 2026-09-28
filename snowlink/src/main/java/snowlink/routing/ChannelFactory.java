package snowlink.routing;

import snowlink.uplink.UplinkChannel;

import java.net.URI;

public interface ChannelFactory {

    UplinkChannel create(URI route);
}
