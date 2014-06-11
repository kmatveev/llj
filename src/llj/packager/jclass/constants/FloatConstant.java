package llj.packager.jclass.constants;

import java.io.IOException;
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

    public static FloatConstant readFrom(ReadableByteChannel bb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
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
