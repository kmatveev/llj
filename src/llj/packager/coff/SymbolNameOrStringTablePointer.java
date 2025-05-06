package llj.packager.coff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SymbolNameOrStringTablePointer extends NameOrStringTablePointer {

    public void read(ByteBuffer source) {
        byte[] bytes = new byte[8];
        source.get(bytes);
        if ((bytes[0] | bytes[1] | bytes[2] | bytes[3]) == 0) {
            type = NameOrStringTablePointer.Type.STRING_TABLE_POINTER;
            ByteBuffer bb = ByteBuffer.wrap(bytes, 4, 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            stringTablePointer = (0xFFFFFFFFL) & bb.getInt();
        } else {
            type = NameOrStringTablePointer.Type.NAME;
            for (int i = 0; i < name.length; i++) name[i] = (char) (bytes[i] & 0xFF);
        }

    }

}
