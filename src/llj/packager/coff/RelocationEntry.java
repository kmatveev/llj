package llj.packager.coff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class RelocationEntry {

    public static final int SIZE = 10;

    public long relativeAddr;
    public long symbolIndex;
    public int relocationType;

    public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readIntoBuffer(in, readBuffer, SIZE);
        readFrom(readBuffer);
    }

    public void readFrom(ByteBuffer readBuffer) {
        relativeAddr = getUnsignedInt(readBuffer);
        symbolIndex = getUnsignedInt(readBuffer);
        relocationType = getUnsignedShort(readBuffer);
    }

    public void writeTo(ByteBuffer writeBuffer) {
        putUnsignedInt(writeBuffer, relativeAddr);
        putUnsignedInt(writeBuffer, symbolIndex);
        putUnsignedShort(writeBuffer, relocationType);
    }


}
