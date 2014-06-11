package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class NameTypePairConstant extends Constant {

    public final ConstantRef<StringConstant> nameRef, typeRef;

    public NameTypePairConstant(ConstantRef<StringConstant> nameRef, ConstantRef<StringConstant> typeRef) {
        this.nameRef = nameRef;
        this.typeRef = typeRef;
    }

    public String resolveName() throws FormatException {
        return nameRef.resolve().value;
    }

    public String resolveType() throws FormatException {
        return typeRef.resolve().value;
    }

    @Override
    public ConstType getType() {
        return ConstType.NAME_TYPE_PAIR;
    }

    public static NameTypePairConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        String part = "";
        try {
            part = "name";
            ConstantRef<StringConstant> nameRef = ConstantRef.readFrom(pool, bb);
            part = "type";
            ConstantRef<StringConstant> typeRef = ConstantRef.readFrom(pool, bb);
            return new NameTypePairConstant(nameRef, typeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + part + " part of NameTypePairConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        i += nameRef.writeTo(bb);
        i += typeRef.writeTo(bb);
        return i;
    }

    @Override
    public boolean isValid() {
        return nameRef.isValid(ConstType.STRING) && typeRef.isValid(ConstType.STRING);
    }

    @Override
    public String toString() {
        try {
            return nameRef.resolve().toString() + typeRef.resolve().toString();
        } catch (Exception e) {
            return "<<error>>";
        }
    }
}
