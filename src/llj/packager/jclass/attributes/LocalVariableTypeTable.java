package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public class LocalVariableTypeTable extends Attribute {

    public static final AttributeType TYPE = AttributeType.LOCAL_VAR_TYPE_TABLE;

    public final ArrayList<LocalVariableTypeDesc> types;

    public LocalVariableTypeTable(ConstantRef<StringConstant> name, ArrayList<LocalVariableTypeDesc> types) {
        super(name);
        this.types = types;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < types.size(); i++) {
            LocalVariableTypeDesc variable = types.get(i);
            if (! variable.isValid()) {
                errors.add("LocalVariableTypeDesc_" + i + " is invalid");
                result = false;
            }
        }
        return result;
    }

    public static LocalVariableTypeTable readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in LocalVariableTypeTable");
        }
        int expectedLength = SIZE_SHORT + numOf * LocalVariableTypeDesc.getSize();
        if (length != expectedLength) throw new ReadException("Incorrect specified attribute length; for " + numOf + " LocalVariableType entries; it should be " + expectedLength + "; specified: " + length);

        ArrayList<LocalVariableTypeDesc> types = new ArrayList<LocalVariableTypeDesc>(numOf);
        for (int j = 0; j < numOf; j++) {
            try {
                LocalVariableTypeDesc type = LocalVariableTypeDesc.readFrom(pool, in);
                types.add(type);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read LocalVariableEntry_" + j + " of LocalVariableTypeTable", e);
            }
        }
        return new LocalVariableTypeTable(name, types);
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + types.size() * LocalVariableTypeDesc.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, types.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < types.size(); i++) {
            numBytes += types.get(i).writeTo(out);
        }
        return numBytes;
    }


    public static class LocalVariableTypeDesc {

        public final int startPC;
        public final int length;
        public final int index;
        public final ConstantRef<StringConstant> nameRef, signatureRef;

        public LocalVariableTypeDesc(ConstantRef<StringConstant> nameRef, ConstantRef<StringConstant> signatureRef, int startPC, int length, int index) {
            this.nameRef = nameRef;
            this.signatureRef = signatureRef;
            this.startPC = startPC;
            this.length = length;
            this.index = index;
        }

        public boolean isValid() {
            return true;
        }

        public static LocalVariableTypeDesc readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "startPC";
                int startPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "length";
                int length = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "nameRef";
                ConstantRef<StringConstant> nameRef = ConstantRef.readFrom(pool, in);
                part = "signatureRef";
                ConstantRef<StringConstant> signatureRef = ConstantRef.readFrom(pool, in);
                part = "index";
                int index = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                return new LocalVariableTypeDesc(nameRef, signatureRef, startPC, length, index);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of LocalVariableTypeDesc");
            }
        }

        public static int getSize() {
            return SIZE_SHORT + SIZE_SHORT + ConstantRef.getSize() + ConstantRef.getSize() + SIZE_SHORT;
        }

        public int writeTo(WritableByteChannel out) throws IOException {
            int numBytes = 0;
            putUnsignedShort(out, startPC, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            putUnsignedShort(out, length, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            numBytes += nameRef.writeTo(out);
            numBytes += signatureRef.writeTo(out);
            putUnsignedShort(out, index, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            return numBytes;
        }


    }

}
