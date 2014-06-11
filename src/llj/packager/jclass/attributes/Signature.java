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

public class Signature extends Attribute {

    public static final AttributeType TYPE = AttributeType.SIGNATURE;

    public final ConstantRef<StringConstant> signature;

    public Signature(ConstantRef<StringConstant> name, ConstantRef<StringConstant> signature) {
        super(name);
        this.signature = signature;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        if (!signature.isValid(Constant.ConstType.STRING)) {
            result = false;
            errors.add("signature doesn't contain valid reference to StringConstant");
        }
        return result;
    }

    public static Signature readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length != ConstantRef.getSize()) throw new ReadException("Incorrect specified attribute length; must be: " + ConstantRef.getSize() + "; specified: " + length);
        try {
            ConstantRef<StringConstant> signature = ConstantRef.readFrom(pool, in);
            return new Signature(name, signature);
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
        return signature.writeTo(out);
    }
}
