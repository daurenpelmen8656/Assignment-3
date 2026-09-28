package snowlink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import snowlink.model.Frame;
import snowlink.model.Priority;
import snowlink.uplink.FailureReason;
import snowlink.uplink.FileUplinkChannel;
import snowlink.uplink.UdpUplinkChannel;
import snowlink.uplink.UplinkException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeChannelsTest {

    @Test
    void fileChannelWritesOneLinePerFrame(@TempDir Path dir) throws Exception {
        Path log = dir.resolve("station").resolve("out.log");
        FileUplinkChannel channel = new FileUplinkChannel(log);

        channel.transmit(new Frame(Priority.NORMAL, "one"));
        channel.transmit(new Frame(Priority.CRITICAL, "two"));

        assertEquals(List.of("NORMAL|one", "CRITICAL|two"), Files.readAllLines(log));
    }

    @Test
    void fileChannelFailureBecomesUplinkException(@TempDir Path dir) {
        FileUplinkChannel channel = new FileUplinkChannel(dir);

        UplinkException e = assertThrows(UplinkException.class,
                () -> channel.transmit(new Frame(Priority.NORMAL, "x")));

        assertEquals(FailureReason.FAULT, e.reason());
    }

    @Test
    void udpChannelSendsOneDatagram() throws Exception {
        try (DatagramSocket receiver = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            receiver.setSoTimeout(3000);
            UdpUplinkChannel channel = new UdpUplinkChannel("127.0.0.1", receiver.getLocalPort());

            channel.transmit(new Frame(Priority.LOW, "hello"));

            DatagramPacket packet = new DatagramPacket(new byte[256], 256);
            receiver.receive(packet);
            assertEquals("LOW|hello", new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void udpChannelRejectsTooBigFrame() {
        UdpUplinkChannel channel = new UdpUplinkChannel("127.0.0.1", 9);

        UplinkException e = assertThrows(UplinkException.class,
                () -> channel.transmit(new Frame(Priority.NORMAL, "x".repeat(2000))));

        assertEquals(FailureReason.REJECTED, e.reason());
    }
}
