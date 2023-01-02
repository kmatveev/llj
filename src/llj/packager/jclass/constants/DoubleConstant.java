package llj.packager.jclass.constants;

import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.getInt;

public class DoubleConstant<E> extends Constant<E> {

    public final double value;

    public DoubleConstant(double value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.DOUBLE;
    }

    public static DoubleConstant readFrom(ReadableByteChannel bb) throws ReadException {
        try {
            // reading from ByteBuffer has the same effect as using Double.longBitsToDouble(bits);
            double value =  BinIOTools.getDouble(bb, ByteOrder.BIG_ENDIAN);
            return new DoubleConstant(value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read DoubleConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        // writing to ByteBuffer has the same effect as using Double.longBitsToDouble(bits);
        BinIOTools.putDouble(bb, value);
        return BinIOTools.SIZE_DOUBLE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        Constant constant = (Constant)obj;
        if (constant.getType() == getType()) {
            DoubleConstant stringConstant = (DoubleConstant)constant;
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
