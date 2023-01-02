package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ModuleInfoConstant extends Constant {

    public final ConstantRef<StringConstant> nameRef;

    public ModuleInfoConstant(ConstantRef<StringConstant> nameRef) {
        this.nameRef = nameRef;
    }

    public Constant.ConstType getType() {
        return ConstType.MODULE;
    }

    public static ModuleInfoConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            ConstantRef<StringConstant> typeRef = ConstantRef.readFrom(pool, bb);
            return new ModuleInfoConstant(typeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a ModuleInfoConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        i += nameRef.writeTo(bb);
        return i;
    }

    @Override
    public boolean isValid() {
        return nameRef.isValid(Constant.ConstType.STRING);
    }

    @Override
    public String toString() {
        try {
            return nameRef.resolve().toString();
        } catch (Exception e) {
            return "<<error>>";
        }
    }

}
