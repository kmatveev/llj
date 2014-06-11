package llj.packager.jclass.constants;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class DoubleConstant<E> extends Constant<E> {

    public final double value;

    public DoubleConstant(double value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.DOUBLE;
    }

    public static DoubleConstant readFrom(ReadableByteChannel bb) {
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
