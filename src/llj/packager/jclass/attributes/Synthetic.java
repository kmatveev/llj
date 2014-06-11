package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class Synthetic extends Attribute {

    public static final AttributeType TYPE = AttributeType.SYNTHETIC;

    public Synthetic(ConstantRef<StringConstant> name) {
        super(name);
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        return result;
    }

    public static Synthetic readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length != 0) throw new ReadException("Incorrect specified attribute length; must be: " + 0 + "; specified: " + length);
        return new Synthetic(name);
    }

    @Override
    public int getValueSize() {
        return 0;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        return 0;
    }
}
