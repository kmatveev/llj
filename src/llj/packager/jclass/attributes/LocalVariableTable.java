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

public class LocalVariableTable extends Attribute {

    public static final AttributeType TYPE = AttributeType.LOCAL_VAR_TABLE;

    public final ArrayList<LocalVariableDesc> variables;

    public LocalVariableTable(ConstantRef<StringConstant> name, ArrayList<LocalVariableDesc> variables) {
        super(name);
        this.variables = variables;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < variables.size(); i++) {
            LocalVariableDesc variable = variables.get(i);
            if (! variable.isValid()) {
                errors.add("LocalVariableDesc_" + i + " is invalid");
                result = false;
            }
        }
        return result;
    }

    public static LocalVariableTable readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in LocalVariableTable");
        }
        int expectedLength = SIZE_SHORT + numOf * LocalVariableDesc.getSize();
        if (length != expectedLength) throw new ReadException("Incorrect specified attribute length; for " + numOf + " LocalVariable entries; it should be " + expectedLength + "; specified: " + length);

        ArrayList<LocalVariableDesc> types = new ArrayList<LocalVariableDesc>(numOf);
        for (int j = 0; j < numOf; j++) {
            try {
                LocalVariableDesc type = LocalVariableDesc.readFrom(pool, in);
                types.add(type);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read LocalVariableEntry_" + j + " of LocalVariableTable", e);
            }
        }
        return new LocalVariableTable(name, types);
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + variables.size() * LocalVariableDesc.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, variables.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < variables.size(); i++) {
            numBytes += variables.get(i).writeTo(out);
        }
        return numBytes;
    }


    public static class LocalVariableDesc {

        public final int startPC;
        public final int length;
        public final int index;
        public final ConstantRef<StringConstant> nameRef, descriptorRef;

        public LocalVariableDesc(ConstantRef<StringConstant> nameRef, ConstantRef<StringConstant> descriptorRef, int startPC, int length, int index) {
            this.nameRef = nameRef;
            this.descriptorRef = descriptorRef;
            this.startPC = startPC;
            this.length = length;
            this.index = index;
        }


        public boolean isValid() {
            return true;
        }

        public static LocalVariableDesc readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "startPC";
                int startPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "length";
                int length = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "nameRef";
                ConstantRef<StringConstant> nameRef = ConstantRef.readFrom(pool, in);
                part = "descriptorRef";
                ConstantRef<StringConstant> descriptorRef = ConstantRef.readFrom(pool, in);
                part = "index";
                int index = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                return new LocalVariableDesc(nameRef, descriptorRef, startPC, length, index);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of LocalVariableDesc");
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
            numBytes += descriptorRef.writeTo(out);
            putUnsignedShort(out, index, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            return numBytes;
        }


    }

}
