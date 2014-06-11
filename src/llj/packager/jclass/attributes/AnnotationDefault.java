package llj.packager.jclass.attributes;

import llj.packager.jclass.attributes.annotations.*;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import static llj.util.BinIOTools.SIZE_CHAR;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;

public class AnnotationDefault extends Attribute {

    public final ElementValue elementValue;

    public AnnotationDefault(ConstantRef<StringConstant> name, ElementValue elementValue) {
        super(name);
        this.elementValue = elementValue;
    }

    @Override
    public AttributeType getType() {
        return null;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        return result;
    }

    public static AnnotationDefault readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_CHAR + "; specified: " + length);
        String part = "";
        try {
            part = "tag";
            char tag = BinIOTools.getUnsignedChar(in);
            length -= SIZE_CHAR;
            part = "elementValue";
            ElementValue elementValue = ElementValue.ElementType.readFrom(tag, pool, in, length);
            return new AnnotationDefault(name, elementValue);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of AnnotationDefault", e);
        }
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + SIZE_CHAR + elementValue.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        BinIOTools.putUnsignedChar(out, elementValue.getTag());
        numBytes += 1;
        numBytes += elementValue.writeTo(out);
        return numBytes;
    }
}
