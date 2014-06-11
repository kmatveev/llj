package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class EnumConstElementValue extends ElementValue {

    public final ConstantRef<StringConstant> typeNameRef, constNameRef;

    public EnumConstElementValue(ConstantRef<StringConstant> typeNameRef, ConstantRef<StringConstant> constNameRef) {
        this.typeNameRef = typeNameRef;
        this.constNameRef = constNameRef;
    }

    @Override
    public ElementType getType() {
        return ElementType.ENUM_CONST;
    }

    public static EnumConstElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
        if (length != ConstantRef.getSize() * 2) throw new ReadException("Reading ElementValue beyond specified limit");
        String part = "";
        try {
            part = "typeNameRef";
            ConstantRef<StringConstant> typeNameRef = ConstantRef.readFrom(pool, in);
            part = "typeNameRef";
            ConstantRef<StringConstant> constNameRef = ConstantRef.readFrom(pool, in);
            return new EnumConstElementValue(typeNameRef, constNameRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of EnumConstElementValue", e);
        }
    }

    public int getSize() {
        return ConstantRef.getSize() * 2;
    }

    @Override
    public int writeTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        numBytes += typeNameRef.writeTo(out);
        numBytes += constNameRef.writeTo(out);
        return numBytes;
    }

    public boolean isValid() {
        return typeNameRef.isValid(Constant.ConstType.STRING) && constNameRef.isValid(Constant.ConstType.STRING);
    }

}
