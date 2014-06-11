package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.nio.channels.ReadableByteChannel;

public class FieldRefConstant<FR, CR> extends ClassMemberRefConstant<FR, CR> {

    public FieldRefConstant(ConstantRef<ClassRefConstant<CR>> classRef, ConstantRef<NameTypePairConstant> nameTypeRef) {
        super(classRef, nameTypeRef);
    }

    public static FieldRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        String part = "";
        try {
            part = "classRef";
            ConstantRef<ClassRefConstant> classRef = ConstantRef.readFrom(pool, bb);
            part = "nameTypeRef";
            ConstantRef<NameTypePairConstant> nameTypeRef = ConstantRef.readFrom(pool, bb);
            return new FieldRefConstant(classRef, nameTypeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + part + " part of FieldRefConstant", e);
        }
    }

    @Override
    public ConstType getType() {
        return ConstType.FIELD_REF;
    }

}
