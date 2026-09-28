package snowlink.uplink;

import snowlink.model.Frame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public final class UdpUplinkChannel implements UplinkChannel {

    private static final int MAX_BYTES = 1400;

    private final String host;
    private final int port;

    public UdpUplinkChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void transmit(Frame frame) throws UplinkException {
        byte[] data = (frame.priority().name() + "|" + frame.payload()).getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_BYTES) {
            throw new UplinkException(FailureReason.REJECTED, "Frame does not fit into one datagram");
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(host);
            socket.send(new DatagramPacket(data, data.length, address, port));
        } catch (UnknownHostException e) {
            throw new UplinkException(FailureReason.UNREACHABLE, "Destination host cannot be resolved", e);
        } catch (IOException e) {
            throw new UplinkException(FailureReason.FAULT, "Datagram could not be sent", e);
        }
    }
}
