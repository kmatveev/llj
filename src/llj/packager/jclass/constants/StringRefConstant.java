package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class StringRefConstant extends Constant {

    public final ConstantRef<StringConstant> stringRef;

    public StringRefConstant(ConstantRef<StringConstant> stringRef) {
        this.stringRef = stringRef;
    }

    public String resolveValue() throws FormatException {
        return stringRef.resolve().value;
    }

    @Override
    public ConstType getType() {
        return ConstType.STRING_REF;
    }

    public static StringRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            ConstantRef<StringConstant> stringRef = ConstantRef.readFrom(pool, bb);
            return new StringRefConstant(stringRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read StringRefConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        return stringRef.writeTo(bb);
    }

    @Override
    public boolean isValid() {
        return stringRef.isValid(ConstType.STRING);
    }

    @Override
    public String toString() {
        try {
            return stringRef.resolve().toString();
        } catch (Exception e) {
            return "<<error>>";
        }
    }

}
