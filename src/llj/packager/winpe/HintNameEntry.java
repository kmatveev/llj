package llj.packager.winpe;

import llj.util.BinIOTools;
import llj.util.BinTools;

import java.nio.ByteBuffer;

public class HintNameEntry {
    
    public int hint;
    public String value;
    
    public static HintNameEntry readFrom(ByteBuffer bb) {
        HintNameEntry entry = new HintNameEntry();
        entry.hint = BinIOTools.getUnsignedShort(bb);
        entry.value = BinTools.readZeroTerminatedAsciiString(bb);
        return entry;
    }
    
    public int getSize() {
        return 2 + value.length(); // TODO possible padding with 0 at the end
    }
}
