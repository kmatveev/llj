package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public abstract class ClassMemberRefConstant<MR, CR> extends Constant<MR> {

    public final ConstantRef<ClassRefConstant<CR> > classRef;
    public final ConstantRef<NameTypePairConstant> nameTypeRef;

    public ClassMemberRefConstant(ConstantRef<ClassRefConstant<CR>> classRef, ConstantRef<NameTypePairConstant> nameTypeRef) {
        this.classRef = classRef;
        this.nameTypeRef = nameTypeRef;
    }

    public ClassRefConstant<CR> resolveClass() throws FormatException {
        return classRef.resolve();
    }

    public NameTypePairConstant resolveNameType() throws FormatException {
        return nameTypeRef.resolve();
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        i += classRef.writeTo(bb);
        i += nameTypeRef.writeTo(bb);
        return i;
    }

    @Override
    public boolean isValid() {
        return classRef.isValid(ConstType.CLASS_REF) && nameTypeRef.isValid(ConstType.NAME_TYPE_PAIR);
    }

    @Override
    public String toString() {
        try {
            return classRef.resolve().toString() + "." + nameTypeRef.resolve().resolveName();
        } catch (Exception e) {
            return "<<error>>";
        }
    }

}
