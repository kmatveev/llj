package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import static llj.util.BinIOTools.getBytes;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.putBytes;

public class UnknownAttribute extends Attribute {

    public static final AttributeType TYPE = AttributeType.UNKNOWN;

    public final byte[] value;

    public UnknownAttribute(ConstantRef<StringConstant> name, byte[] value) {
        super(name);
        this.value = value;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        return result;
    }

    public static UnknownAttribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        try {
            byte[] value = getBytes(in, length);
            return new UnknownAttribute(name, value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read UnknownConstant");
        }
    }

    @Override
    public int getValueSize() {
        return value.length;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        putBytes(out, value);
        return value.length;
    }
}
