package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ClassInfoIndexElementValue extends ElementValue {

    public final ConstantRef<Constant> ref;

    public ClassInfoIndexElementValue(ConstantRef<Constant> ref) {
        this.ref = ref;
    }

    @Override
    public ElementValue.ElementType getType() {
        return ElementValue.ElementType.CLASS_INFO;
    }

    public static ClassInfoIndexElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
        if (length != ConstantRef.getSize()) throw new ReadException("Reading ElementValue beyond specified limit");
        try {
            ConstantRef ref = ConstantRef.readFrom(pool, in);
            return new ClassInfoIndexElementValue(ref);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read constRef part of ConstIndexElementValue", e);
        }
    }

    public int getSize() {
        return ConstantRef.getSize();
    }

    public boolean isValid() {
        return ref.isValid(Constant.ConstType.STRING);
    }

    @Override
    public int writeTo(WritableByteChannel out) throws IOException {
        return ref.writeTo(out);
    }
}
