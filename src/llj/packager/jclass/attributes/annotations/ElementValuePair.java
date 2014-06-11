package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.SIZE_CHAR;
import static llj.util.BinIOTools.SIZE_SHORT;

public class ElementValuePair {

    public final ConstantRef<StringConstant> name;
    public final ElementValue value;

    public ElementValuePair(ConstantRef<StringConstant> name, ElementValue value) {
        this.name = name;
        this.value = value;
    }

    public static ElementValuePair readFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
        String part = "";
        try {
            part = "name";
            if (length < ConstantRef.getSize()) throw new ReadException("Attempt to read name beyond provided limit");
            ConstantRef<StringConstant> name = ConstantRef.readFrom(pool, in);
            length -= ConstantRef.getSize();
            part = "tag";
            if (length < SIZE_SHORT) throw new ReadException("Attempt to read tag beyond provided limit");
            char tag = BinIOTools.getUnsignedChar(in);
            length -= SIZE_CHAR;
            part = "value";
            ElementValue value = ElementValue.ElementType.readFrom(tag, pool, in, length);
            return new ElementValuePair(name, value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of ElementValuePair", e);
        }
    }

    public int getSize() {
        return ConstantRef.getSize() + SIZE_CHAR + value.getSize();
    }

    public int writeTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        numBytes += name.writeTo(out);
        BinIOTools.putUnsignedChar(out, value.getTag());
        numBytes += 1;
        numBytes += value.writeTo(out);
        return numBytes;
    }

}
