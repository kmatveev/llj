package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.SIZE_INT;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.putInt;

public class IntegerConstant<E> extends Constant<E> {

    public final int value;

    public IntegerConstant(int value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.INTEGER;
    }

    public static IntegerConstant readFrom(ReadableByteChannel bb) throws ReadException {
        try {
            int value = getInt(bb, ByteOrder.BIG_ENDIAN);
            return new IntegerConstant(value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read IntegerConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        putInt(bb, value, ByteOrder.BIG_ENDIAN);
        return SIZE_INT;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        Constant constant = (Constant)obj;
        if (constant.getType() == getType()) {
            IntegerConstant stringConstant = (IntegerConstant)constant;
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
