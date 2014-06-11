package llj.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class DummyWritableChannel implements WritableByteChannel {

    public static final DummyWritableChannel instance = new DummyWritableChannel();

    @Override
    public int write(ByteBuffer src) throws IOException {
        int i = src.remaining();
        while (src.hasRemaining()) {
            src.get();
        }
        return i;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {

    }
}
