package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ConstIndexElementValue extends ElementValue {

    public final ConstantRef<Constant> ref;
    public final char tag;

    public ConstIndexElementValue(char tag, ConstantRef<Constant> ref) {
        this.ref = ref;
        this.tag = tag;
    }

    @Override
    public ElementType getType() {
        return ElementType.CONST;
    }

    public static ConstIndexElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
        if (length < ConstantRef.getSize()) throw new ReadException("Reading ElementValue beyond specified limit");
        try {
            ConstantRef ref = ConstantRef.readFrom(pool, in);
            return new ConstIndexElementValue(tag, ref);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read constRef part of ConstIndexElementValue", e);
        }
    }

    public int getSize() {
        return ConstantRef.getSize();
    }

    @Override
    public int writeTo(WritableByteChannel out) throws IOException {
        return 0;  //TODO
    }

    @Override
    public char getTag() {
        return tag;
    }

    public boolean isValid() {
        return true; // TODO
    }
}
