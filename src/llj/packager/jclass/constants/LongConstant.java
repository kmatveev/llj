package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.SIZE_LONG;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getLong;
import static llj.util.BinIOTools.putLong;

public class LongConstant<E> extends Constant<E> {

    public long value;

    public LongConstant(long value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.LONG;
    }

    public static LongConstant readFrom(ReadableByteChannel bb) throws ReadException {
        try {
            long value = getLong(bb, ByteOrder.BIG_ENDIAN);
            return new LongConstant(value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read LongConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        putLong(bb, value, ByteOrder.BIG_ENDIAN);
        return SIZE_LONG;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        Constant constant = (Constant)obj;
        if (constant.getType() == getType()) {
            LongConstant stringConstant = (LongConstant)constant;
            if (stringConstant.value == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
