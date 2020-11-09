package llj.packager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public interface Format {

    public static final int BYTE = 1, WORD = 2, DWORD = 4;

    public default void writeTo(WritableByteChannel out) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(getSize());
        writeTo(bb);
        out.write(bb);
    }

    /**
     * This method must write the same amount of bytes as returned by getSize()
     * @param out
     */
    public void writeTo(ByteBuffer out);

    public int getSize();

    public String getStringValue();


}
