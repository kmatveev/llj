package llj.packager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class EmptyFormat implements Format {

    private final int size;

    public EmptyFormat(int size) {
        this.size = size;
    }

    @Override
    public void writeTo(WritableByteChannel out) throws IOException {
        out.write(ByteBuffer.allocate(size));
    }

    @Override
    public void writeTo(ByteBuffer out) {
        out.put(ByteBuffer.allocate(size));
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
