package llj.packager.winpe;

import java.nio.ByteBuffer;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.putUnsignedInt;

public class DirectoryEntry {

    public static final int SIZE = 8;
    
    long VirtualAddress;  // this field is actually RVA
    long Size;
    String name;

    public void readFrom(ByteBuffer readBuffer) {
        VirtualAddress = getUnsignedInt(readBuffer);
        Size = getUnsignedInt(readBuffer);
    }

    public void writeTo(ByteBuffer writeBuffer) {
        putUnsignedInt(writeBuffer, VirtualAddress);
        putUnsignedInt(writeBuffer, Size);
    }

}
