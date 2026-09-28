package snowlink.app;

import snowlink.routing.ChannelRegistry;
import snowlink.routing.UnsupportedRouteException;
import snowlink.satellite.SatelliteUplinkAdapter;
import snowlink.uplink.FileUplinkChannel;
import snowlink.uplink.UdpUplinkChannel;
import vendor.satmodem.SatelliteModem;

import java.net.URI;
import java.nio.file.Path;

public final class DefaultChannels {

    private DefaultChannels() {
    }

    public static ChannelRegistry create() {
        return create(new SatelliteModem(8));
    }

    public static ChannelRegistry create(SatelliteModem modem) {
        return new ChannelRegistry()
                .register("file", route -> new FileUplinkChannel(pathOf(route)))
                .register("udp", route -> new UdpUplinkChannel(hostOf(route), portOf(route)))
                .register("sat", route -> new SatelliteUplinkAdapter(modem, hostOf(route)));
    }

    private static Path pathOf(URI route) {
        try {
            return Path.of(route);
        } catch (RuntimeException e) {
            throw new UnsupportedRouteException("File route must look like file:///path/to/log, got: " + route);
        }
    }

    private static String hostOf(URI route) {
        if (route.getHost() == null) {
            throw new UnsupportedRouteException("Route needs a host part, got: " + route);
        }
        return route.getHost();
    }

    private static int portOf(URI route) {
        if (route.getPort() < 0) {
            throw new UnsupportedRouteException("Route needs a port, got: " + route);
        }
        return route.getPort();
    }
}
