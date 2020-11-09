package llj.packager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class RawFormat implements Format {

    private final ByteBuffer rawData;

    public RawFormat(ByteBuffer rawData) {
        this.rawData = rawData;
    }

    public RawFormat(int size) {
        rawData = ByteBuffer.allocate(size);
    }

    @Override
    public void writeTo(WritableByteChannel out) throws IOException {
        out.write(rawData);
    }

    @Override
    public void writeTo(ByteBuffer out) {
        out.put(rawData);
    }

    @Override
    public int getSize() {
        return rawData.capacity();
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
