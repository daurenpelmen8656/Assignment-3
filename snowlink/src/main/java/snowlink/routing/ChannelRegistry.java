package snowlink.routing;

import snowlink.uplink.UplinkChannel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public final class ChannelRegistry {

    private final Map<String, ChannelFactory> factories = new HashMap<>();

    public ChannelRegistry register(String scheme, ChannelFactory factory) {
        factories.put(scheme.toLowerCase(), factory);
        return this;
    }

    public UplinkChannel open(String route) {
        if (route == null || route.isBlank()) {
            throw new UnsupportedRouteException("Route must not be empty");
        }
        URI uri;
        try {
            uri = new URI(route.trim());
        } catch (URISyntaxException e) {
            throw new UnsupportedRouteException("Route is not a valid URI: " + route);
        }
        if (uri.getScheme() == null) {
            throw new UnsupportedRouteException("Route has no scheme: " + route);
        }
        ChannelFactory factory = factories.get(uri.getScheme().toLowerCase());
        if (factory == null) {
            throw new UnsupportedRouteException("No channel is registered for route: " + route);
        }
        return factory.create(uri);
    }
}
