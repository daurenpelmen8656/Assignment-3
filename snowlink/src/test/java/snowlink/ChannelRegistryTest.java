package snowlink;

import org.junit.jupiter.api.Test;
import snowlink.app.DefaultChannels;
import snowlink.routing.ChannelRegistry;
import snowlink.routing.UnsupportedRouteException;
import snowlink.satellite.SatelliteUplinkAdapter;
import snowlink.uplink.FileUplinkChannel;
import snowlink.uplink.UdpUplinkChannel;
import snowlink.uplink.UplinkChannel;
import vendor.satmodem.SatelliteModem;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ChannelRegistryTest {

    private final UplinkChannel first = mock(UplinkChannel.class);
    private final UplinkChannel second = mock(UplinkChannel.class);

    @Test
    void schemeDecidesWhichChannelIsReturned() {
        ChannelRegistry registry = new ChannelRegistry()
                .register("aaa", route -> first)
                .register("bbb", route -> second);

        assertSame(first, registry.open("aaa://somewhere"));
        assertSame(second, registry.open("bbb://elsewhere"));
    }

    @Test
    void schemeIgnoresCase() {
        ChannelRegistry registry = new ChannelRegistry().register("aaa", route -> first);

        assertSame(first, registry.open("AAA://x"));
    }

    @Test
    void unknownSchemeIsRejected() {
        ChannelRegistry registry = new ChannelRegistry().register("aaa", route -> first);

        assertThrows(UnsupportedRouteException.class, () -> registry.open("zzz://x"));
    }

    @Test
    void badRoutesAreRejected() {
        ChannelRegistry registry = new ChannelRegistry().register("aaa", route -> first);

        assertThrows(UnsupportedRouteException.class, () -> registry.open(null));
        assertThrows(UnsupportedRouteException.class, () -> registry.open("  "));
        assertThrows(UnsupportedRouteException.class, () -> registry.open("not a uri"));
        assertThrows(UnsupportedRouteException.class, () -> registry.open("no-scheme-here"));
    }

    @Test
    void defaultRegistryPicksRightChannel() {
        ChannelRegistry registry = DefaultChannels.create(mock(SatelliteModem.class));

        assertInstanceOf(FileUplinkChannel.class, registry.open("file:///tmp/snowlink-test.log"));
        assertInstanceOf(UdpUplinkChannel.class, registry.open("udp://127.0.0.1:9999"));
        assertInstanceOf(SatelliteUplinkAdapter.class, registry.open("sat://ops-7"));
    }

    @Test
    void defaultRegistryRejectsIncompleteRoutes() {
        ChannelRegistry registry = DefaultChannels.create(mock(SatelliteModem.class));

        assertThrows(UnsupportedRouteException.class, () -> registry.open("sat:///"));
        assertThrows(UnsupportedRouteException.class, () -> registry.open("udp://127.0.0.1"));
        assertThrows(UnsupportedRouteException.class, () -> registry.open("file:relative/path"));
    }
}
