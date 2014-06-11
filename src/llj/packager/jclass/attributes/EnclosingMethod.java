package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ClassRefConstant;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.NameTypePairConstant;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class EnclosingMethod extends Attribute {

    public static final AttributeType TYPE = AttributeType.ENCLOSING_METHOD;

    public final ConstantRef<ClassRefConstant> classRef;
    public final ConstantRef<NameTypePairConstant> methodRef;

    public EnclosingMethod(ConstantRef<StringConstant> name, ConstantRef<ClassRefConstant> classRef, ConstantRef<NameTypePairConstant> methodRef) {
        super(name);
        this.classRef = classRef;
        this.methodRef = methodRef;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        if (!classRef.isValid(Constant.ConstType.CLASS_REF)) {
            errors.add("classRef is not a valid reference to ClassRefConstant");
            result = false;
        }
        if (!methodRef.isValid(Constant.ConstType.NAME_TYPE_PAIR)) {
            errors.add("methodRef is not a valid reference to NameTypePairConstant");
            result = false;
        }
        return result;
    }

    public static EnclosingMethod readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length != ConstantRef.getSize() * 2) throw new ReadException("Incorrect specified attribute length; must be: " + ConstantRef.getSize() + "; specified: " + length);
        String part = "";
        try {
            part = "classRef";
            ConstantRef<ClassRefConstant> classRef = ConstantRef.readFrom(pool, in);
            part = "methodRef";
            ConstantRef<NameTypePairConstant> methodRef = ConstantRef.readFrom(pool, in);
            return new EnclosingMethod(name, classRef, methodRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of EnclosingMethod");
        }
    }

    @Override
    public int getValueSize() {
        return ConstantRef.getSize() * 2;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        numBytes += classRef.writeTo(out);
        numBytes += methodRef.writeTo(out);
        return numBytes;
    }
}
