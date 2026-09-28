package snowlink.uplink;

import snowlink.model.Frame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileUplinkChannel implements UplinkChannel {

    private final Path file;

    public FileUplinkChannel(Path file) {
        this.file = file;
    }

    @Override
    public void transmit(Frame frame) throws UplinkException {
        String line = frame.priority().name() + "|" + frame.payload() + System.lineSeparator();
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UplinkException(FailureReason.FAULT, "Could not write frame to local log", e);
        }
    }
}
