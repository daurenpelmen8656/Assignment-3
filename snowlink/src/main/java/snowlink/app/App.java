package snowlink.app;

import snowlink.model.Observation;
import snowlink.report.AvalancheAlertReporter;
import snowlink.report.DiagnosticsReporter;
import snowlink.report.RoutineReporter;
import snowlink.report.StationReporter;
import snowlink.routing.ChannelRegistry;
import snowlink.routing.UnsupportedRouteException;
import snowlink.uplink.UplinkChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public final class App {

    public static void main(String[] args) throws IOException {
        Path log = Files.createTempFile("snowlink-", ".log");
        List<String> routes;
        if (args.length > 0) {
            routes = List.of(args);
        } else {
            routes = List.of(log.toUri().toString(), "udp://127.0.0.1:9999", "sat://ops-7", "ftp://nowhere");
        }

        ChannelRegistry registry = DefaultChannels.create();
        Observation observation = new Observation("col-de-bel-1", Instant.now(), 182.5, -7.3, 14.2, 4, 71);

        for (String route : routes) {
            System.out.println("== " + route);
            UplinkChannel channel;
            try {
                channel = registry.open(route);
            } catch (UnsupportedRouteException e) {
                System.out.println("   skipped: " + e.getMessage());
                continue;
            }
            List<StationReporter> reporters = List.of(
                    new RoutineReporter(channel),
                    new AvalancheAlertReporter(channel),
                    new DiagnosticsReporter(channel));
            for (StationReporter reporter : reporters) {
                System.out.println("   " + reporter.getClass().getSimpleName() + " -> " + reporter.report(observation));
            }
        }
        System.out.println("(local log written to " + log + ")");
    }
}
