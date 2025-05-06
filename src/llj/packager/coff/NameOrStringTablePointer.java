package llj.packager.coff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class NameOrStringTablePointer {

    public static final int SIZE = 8;

    public enum Type {NAME, STRING_TABLE_POINTER };

    public Type type;

    public final char[] name = new char[SIZE];

    public long stringTablePointer;

    // TODO
    public void write(ByteBuffer dest) {
        throw new UnsupportedOperationException();
    }

    public int getSize() {
        return SIZE;
    }
    
    public String getStringVal() {
        if (type == Type.NAME) {
            return new String(name);
        } else {
            return "StringTableBase+" + stringTablePointer;
        }
    }
}
