package llj.packager.jclass.constants;

import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FloatConstant<E> extends Constant<E> {

    public final float value;

    public FloatConstant(float value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.FLOAT;
    }

    public static FloatConstant readFrom(ReadableByteChannel bb) throws ReadException {
        try {
            float val;
//            int bits = BinIOTools.getInt(bb, ByteOrder.BIG_ENDIAN);
//        if (bits == 0x7f800000) {
//            val = Float.POSITIVE_INFINITY;
//        } else if (bits == 0xff800000) {
//            val = Float.NEGATIVE_INFINITY;
//        } else if (((bits >= 0x7f800001) && (bits <= 0x7fffffff )) || ((bits >= 0xff800001) && (bits <= 0xffffffff))) {
//            val = Float.NaN;
//        } else {
//            // TODO
//        }
            
            // reading from ByteBuffer has the same effect as using Float.intBitsToFloat(bits);
            val = BinIOTools.getFloat(bb, ByteOrder.BIG_ENDIAN);
            return new FloatConstant(val);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read FloatConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        // Writing to ByteBuffer has same effect as using Float.floatToRawIntBits
        BinIOTools.putFloat(bb, value, ByteOrder.BIG_ENDIAN);
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        Constant constant = (Constant)obj;
        if (constant.getType() == getType()) {
            FloatConstant stringConstant = (FloatConstant)constant;
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
