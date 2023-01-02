package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.*;

public class DynamicConstant extends Constant {
    
    public final int bootstrapMethodAttrIndex;
    public final ConstantRef<NameTypePairConstant> nameTypeRef;

    public DynamicConstant(int bootstrapMethodAttrIndex, ConstantRef<NameTypePairConstant> nameTypeRef) {
        this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
        this.nameTypeRef = nameTypeRef;
    }

    public static DynamicConstant readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        String part = "";
        try {
            part = "bootstrapMethodAttrIndex";
            int bootstrapMethodAttrIndex = getUnsignedShort(bb, ByteOrder.BIG_ENDIAN);
            part = "nameTypeRef";
            ConstantRef<NameTypePairConstant> nameTypeRef = ConstantRef.readFrom(pool, bb);
            return new DynamicConstant(bootstrapMethodAttrIndex, nameTypeRef);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + part + " part of DynamicConstant", e);
        }
    }
    

    @Override
    public ConstType getType() {
        return ConstType.DYNAMIC;
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        int i = 0;
        putUnsignedShort(bb, bootstrapMethodAttrIndex, ByteOrder.BIG_ENDIAN);
        i += SIZE_SHORT;
        i += nameTypeRef.writeTo(bb);
        return i;
    }

    @Override
    public boolean isValid() {
        // TODO check bootstrapMethodAttrIndex
        return nameTypeRef.isValid(ConstType.NAME_TYPE_PAIR);
    }
}
