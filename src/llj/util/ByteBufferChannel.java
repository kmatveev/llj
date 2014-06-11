package llj.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class ByteBufferChannel implements ByteChannel {

    private final ByteBuffer buffer;

    public ByteBufferChannel(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return transfer(buffer, dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return transfer(src, buffer);

    }

    public static int transfer(ByteBuffer src, ByteBuffer dst) {
        int l = src.limit();
        int transferred;
        if (dst.remaining() < src.remaining()) {
            src.limit(src.position() + dst.remaining());
            transferred = dst.remaining();
            dst.put(src);
            src.limit(l);
        } else {
            transferred = src.remaining();
            dst.put(src);
        }
        return transferred;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
