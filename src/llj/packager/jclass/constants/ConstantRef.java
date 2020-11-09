package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static llj.util.BinIOTools.SIZE_BYTE;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedShort;

public class ConstantRef<E extends Constant> {

    final int index;
    public final ConstantPool pool;

    public ConstantRef(ConstantPool pool, int index) {
        this.pool = pool;
        this.index = index;
    }

    public E resolve() throws ResolveException {
        if (!isNullRef()) {
            E referent = pool.get(index);
            return referent;
        } else {
            return null;
        }
    }

    public boolean isNullRef() {
        return index == 0;
    }

    public static <F extends Constant> ConstantRef<F> readFrom(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            int index = getUnsignedShort(bb, ByteOrder.BIG_ENDIAN);
            return new ConstantRef<F>(pool, index);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a reference", e);
        }
    }

    public static int getSize() {
        return SIZE_SHORT;
    }

    public static <F extends Constant> ConstantRef<F> readFromCompact(ConstantPool pool, ReadableByteChannel bb) throws ReadException {
        try {
            int index = getUnsignedByte(bb);
            return new ConstantRef<F>(pool, index);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a reference", e);
        }
    }

    public int writeTo(WritableByteChannel bb) throws IOException {
        putUnsignedShort(bb, index, ByteOrder.BIG_ENDIAN);
        return SIZE_SHORT;
    }

    public int writeToCompact(WritableByteChannel bb) throws IOException {
        if (index > 255) throw new IllegalStateException();
        putUnsignedByte(bb, (short)index);
        return SIZE_BYTE;
    }

    public boolean isValid(Constant.ConstType type) {
        if (pool == null) return false;
        try {
            Constant referent = resolve();
            if (referent != null) {
                return referent.getType() == type;
            } else {
                return false;
            }
        } catch (ResolveException e) {
            return false;
        }
    }
    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        ConstantRef ref = (ConstantRef)obj;
        return (this.pool == ref.pool) && (this.index == ref.index);
    }

}
