package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.nio.channels.ReadableByteChannel;

public class MethodRefConstant<MR, CR> extends ClassMemberRefConstant<MR, CR> {

    public MethodRefConstant(ConstantRef<ClassRefConstant<CR>> classRef, ConstantRef<NameTypePairConstant> nameTypeRef) {
        super(classRef, nameTypeRef);
    }

    public static MethodRefConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        String part = "";
        try {
            part = "classRef";
            ConstantRef<ClassRefConstant> classRef = ConstantRef.readFrom(pool, bb);
            part = "nameTypeRef";
            ConstantRef<NameTypePairConstant> nameTypeRef = ConstantRef.readFrom(pool, bb);
            return new MethodRefConstant(classRef, nameTypeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + part + " part of MethodRefConstant", e);
        }
    }

    @Override
    public ConstType getType() {
        return ConstType.METHOD_REF;
    }

}
