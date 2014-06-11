package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;


public class ConstantValue extends Attribute {

    public final ConstantRef<? extends Constant> ref;

    public static final AttributeType TYPE = AttributeType.CONSTANT_VALUE;

    public ConstantValue(ConstantRef<StringConstant> name, ConstantRef<? extends Constant> ref) {
        super(name);
        this.ref = ref;
    }

    public AttributeType getType() {
        return TYPE;
    }

    public static ConstantValue readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length != ConstantRef.getSize()) throw new ReadException("Incorrect specified attribute length; must be: " + ConstantRef.getSize() + "; specified: " + length);
        try {
            ConstantRef ref = ConstantRef.readFrom(pool, in);
            return new ConstantValue(name, ref);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read UnknownConstant");
        }
    }

    @Override
    public int getValueSize() {
        return ConstantRef.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        return ref.writeTo(out);
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        if (ref.isValid(Constant.ConstType.DOUBLE)
                || ref.isValid(Constant.ConstType.FLOAT)
                || ref.isValid(Constant.ConstType.INTEGER)
                || ref.isValid(Constant.ConstType.LONG)) {
            // nothing, valid ref
        } else {
            errors.add("Attribute references a constant of wrong type");
            result = false;
        }

        return result;
    }

}
