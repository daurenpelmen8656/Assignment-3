# SnowLink: Bridge + Adapter

Uplink layer for remote alpine snow stations. Report kinds and transport channels vary independently (Bridge);
the vendor's satellite modem SDK, which cannot implement our interface, is brought into the channel hierarchy (Adapter).

Complexity module: **dynamic implementor selection** (the route string decides which channel is used).

## Build, test, run

Needs JDK 17+ and Maven.

```
mvn test                 # builds and runs all JUnit 5 tests
mvn -q compile exec:java # runs the demo (snowlink.app.App)
```

Demo with your own routes: `mvn -q compile exec:java -Dexec.args="file:///tmp/a.log udp://127.0.0.1:9999 sat://ops-7"`

Route formats: `file:///absolute/path.log`, `udp://host:port`, `sat://recipient-id`.

## Where things are

| Role | Class |
|---|---|
| Abstraction | `snowlink.report.StationReporter` |
| Refined Abstractions | `RoutineReporter`, `AvalancheAlertReporter`, `DiagnosticsReporter` |
| Implementor | `snowlink.uplink.UplinkChannel` (+ `UplinkException`, `FailureReason`) |
| Concrete Implementors | `FileUplinkChannel`, `UdpUplinkChannel` |
| Adapter | `snowlink.satellite.SatelliteUplinkAdapter` |
| Adaptee (vendor, not modified) | `vendor.satmodem.SatelliteModem` |
| Dynamic selection | `snowlink.routing.ChannelRegistry`, wired in `snowlink.app.DefaultChannels` |

Docs: `docs/uml-class-diagram.png` (source: `.puml`), `docs/DESIGN_RATIONALE.pdf`.

## Tests

- `ReporterDelegationTest`: normal delegation for all three refined abstractions (channel mocked)
- `ReporterRetryPolicyTest`: retry behaviour driven only by `FailureReason`
- `SatelliteUplinkAdapterTest`: vendor modem mocked; parameter/priority translation and every status code to `UplinkException`
- `ChannelRegistryTest`: implementor chosen from the route, including the real wiring
- `OpenClosedTest`: a new abstraction and a new implementor added without touching production code
- `NativeChannelsTest`: file and UDP channels
