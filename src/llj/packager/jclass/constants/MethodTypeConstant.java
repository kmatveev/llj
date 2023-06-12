package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class MethodTypeConstant extends Constant {

    public final ConstantRef<StringConstant> typeRef;

    public MethodTypeConstant(ConstantRef<StringConstant> typeRef) {
        this.typeRef = typeRef;
    }

    public ConstType getType() {
        return ConstType.METHOD_TYPE;
    }

    public static MethodTypeConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            ConstantRef<StringConstant> typeRef = ConstantRef.readFrom(pool, bb);
            return new MethodTypeConstant(typeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a MethodTypeConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        i += typeRef.writeTo(bb);
        return i;
    }

    @Override
    public boolean isValid() {
        return typeRef.isValid(ConstType.STRING);
    }

    @Override
    public String toString() {
        try {
            return typeRef.resolve().toString();
        } catch (Exception e) {
            return "<<error>>";
        }
    }

    public String resolveValue() throws FormatException {
        return typeRef.resolve().value;
    }

    
}
