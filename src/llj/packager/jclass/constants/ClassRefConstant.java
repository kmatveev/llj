package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ClassRefConstant<CR> extends Constant<CR> {

    public final ConstantRef<StringConstant> nameRef;

    public ClassRefConstant(ConstantRef<StringConstant> ref) {
        this.nameRef = ref;
    }

    public String resolveName() throws FormatException {
        return nameRef.resolve().value;
    }

    @Override
    public ConstType getType() {
        return ConstType.CLASS_REF;
    }

    public static ClassRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            ConstantRef<StringConstant> nameRef = ConstantRef.readFrom(pool, bb);
            return new ClassRefConstant(nameRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read ClassRefConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        return nameRef.writeTo(bb);
    }

    @Override
    public boolean isValid() {
        return nameRef.isValid(ConstType.STRING);
    }

    @Override
    public String toString() {
        try {
            return nameRef.resolve().value;
        } catch (Exception e) {
            return "<<error>>";
        }
    }
}
