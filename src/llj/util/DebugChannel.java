package llj.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class DebugChannel implements ReadableByteChannel {

    public final ReadableByteChannel wrappedChannel;
    public int position;

    public DebugChannel(ReadableByteChannel wrappedChannel) {
        this.wrappedChannel = wrappedChannel;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        System.out.println("Position: " + position + "; Reading " + dst.remaining() + " bytes.");
        position += dst.remaining();
        int result = wrappedChannel.read(dst);
        if (dst.remaining() > 0) {
            System.out.println("Bytes were not read! remaining: " + dst.remaining());
        }
        return result;
    }

    @Override
    public boolean isOpen() {
        return wrappedChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        wrappedChannel.close();
    }
}
