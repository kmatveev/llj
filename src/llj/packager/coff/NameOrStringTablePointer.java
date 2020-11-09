package llj.packager.coff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NameOrStringTablePointer {

    public enum Type {NAME, STRING_TABLE_POINTER };

    public Type type;

    public final char[] name = new char[8];

    public long stringTablePointer;

    public void read(ByteBuffer source) {
        byte[] bytes = new byte[8];
        source.get(bytes);
        if ((bytes[0] | bytes[1] | bytes[2] | bytes[3]) == 0) {
            type = Type.STRING_TABLE_POINTER;
            ByteBuffer bb = ByteBuffer.wrap(bytes, 4, 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            stringTablePointer = (0xFFFFFFFFL) & bb.getInt();
        } else {
            type = Type.NAME;
            for (int i = 0; i < name.length; i++) name[i] = (char) (bytes[i] & 0xFF);
        }

    }

    public void write(ByteBuffer dest) {
        throw new UnsupportedOperationException();
    }


}
