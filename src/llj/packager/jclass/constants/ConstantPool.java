package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import static llj.util.BinIOTools.SIZE_BYTE;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedShort;

public class ConstantPool {

    private final ArrayList<Constant> constants = new ArrayList<Constant>();

    public <E extends Constant> E get(int index) throws ResolveException {
        try {
            // since we store constants in list, and others refer with index starting with 1, we use (index - 1) here
            Constant referent = constants.get(index - 1);
            return (E)referent;
        } catch (IndexOutOfBoundsException e) {
            throw new ResolveException(e);
        }
    }

    public void clear() {
        constants.clear();
    }

    public void readFrom(ReadableByteChannel readBuffer) throws ReadException {
        String part = "";
        try {
            part = "size";
            int size = getUnsignedShort(readBuffer, ByteOrder.BIG_ENDIAN);
            // yes, that's not a bug: JVM spec says use (size - 1)
            for (int i = 0; i < (size - 1); i++) {
                part = "code of constant #" + i;
                short code = getUnsignedByte(readBuffer);
                Constant.ConstType type = Constant.ConstType.getByCode(code);
                part = "value of constant #" + i;
                Constant value = type.readFrom(this, readBuffer);
                constants.add(value);
                // sanity check
                if (value.getType() != type) throw new RuntimeException();
                // Surprised? Me too. JVM spec has this stuff that long and double constants "occupy" two slots in const table.
                if (type == Constant.ConstType.DOUBLE || type == Constant.ConstType.LONG) {
                    i++;
                    constants.add(new ConstantPlaceholder());
                }
            }
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " from constant pool", e);
        }
    }

    public int writeTo(WritableByteChannel writeBuffer) throws IOException {
        // yes, that's not a bug: JVM spec says write (size + 1)
        putUnsignedShort(writeBuffer, constants.size() + 1, ByteOrder.BIG_ENDIAN);
        int bytesProduced = SIZE_SHORT;
        for (Constant constant : constants) {
            putUnsignedByte(writeBuffer, (short)constant.getType().code);
            bytesProduced += SIZE_BYTE;
            bytesProduced += constant.writeTo(writeBuffer);
        }
        return bytesProduced;
    }

    public boolean hasCompactRefs() {
        return constants.size() < 256;
    }

    public boolean hasCompactStrings() {
        for (Constant constant : constants) {
            if (constant.getType() == Constant.ConstType.STRING) {
                StringConstant stringConstant = (StringConstant)constant;
                if (stringConstant.value.length() > 255) return false;
            }
        }
        return true;
    }

    public <E extends Constant> ConstantRef<E> makeRef(E constant) {
        int i = constants.indexOf(constant);
        if (i < 0) throw new IllegalArgumentException("Provided constant is not a member of a pool");
        ConstantRef<E> ref = new ConstantRef<E>(this, i + 1);
        return ref;
    }

    public <E extends Constant> ConstantRef<E> addOrGetExisting(E candidate) {
        int i = constants.indexOf(candidate);
        if (i >= 0) {
            return new ConstantRef<E>(this, i + 1);
        } else {
            constants.add(candidate);
            return new ConstantRef<E>(this, constants.size());
        }
    }

    public int size() {
        return constants.size();
    }

    public ConstantRef<StringConstant> makeString(String value) {
        return addOrGetExisting(new StringConstant(value));
    }

    public ConstantRef<StringRefConstant> makeStringRef(String value) {
        ConstantRef<StringConstant> ref = makeString(value);
        return addOrGetExisting(new StringRefConstant(ref));
    }

    public ConstantRef<ClassRefConstant> makeClassRef(String value) {
        ConstantRef<StringConstant> ref = makeString(value);
        return addOrGetExisting(new ClassRefConstant(ref));
    }

    public ConstantRef<NameTypePairConstant> makeNameValueRef(String name, String type) {
        ConstantRef<StringConstant> nameRef = makeString(name);
        ConstantRef<StringConstant> typeRef = makeString(type);
        return addOrGetExisting(new NameTypePairConstant(nameRef, typeRef));
    }

}
