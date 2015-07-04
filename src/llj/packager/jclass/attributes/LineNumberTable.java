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

public class LineNumberTable extends Attribute {

    public static final AttributeType TYPE = AttributeType.LINE_NUMBER_TABLE;

    public final ArrayList<LineNumberDesc> lineNumbers;

    public LineNumberTable(ConstantRef<StringConstant> name, ArrayList<LineNumberDesc> lineNumbers) {
        super(name);
        this.lineNumbers = lineNumbers;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < lineNumbers.size(); i++) {
            LineNumberDesc lineNum = lineNumbers.get(i);
            if (! lineNum.isValid()) {
                errors.add("LineNumberDesc_" + i + " is not valid");
                result = false;
            }
            if (i == 0) {
                if (lineNum.startPC != 0) {
                    errors.add("LineNumberDesc_" + i + " startPC is not 0");
                    result = false;
                }
            } else {
                if (lineNum.startPC <= (lineNumbers.get(i - 1).startPC)) {
                    errors.add("LineNumberDesc_" + i + " startPC is not greater than previous line startPC");
                    result = false;
                }
            }
        }
        return result;
    }

    public static LineNumberTable readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in LocalVariableTable");
        }
        int expectedLength = SIZE_SHORT + numOf * LineNumberDesc.getSize();
        if (length != expectedLength) throw new ReadException("Incorrect specified attribute length; for " + numOf + " LineNumber entries; it should be " + expectedLength + "; specified: " + length);

        ArrayList<LineNumberDesc> types = new ArrayList<LineNumberDesc>(numOf);
        for (int j = 0; j < numOf; j++) {
            try {
                LineNumberDesc type = LineNumberDesc.readFrom(pool, in);
                types.add(type);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read LineNumberEntry_" + j + " of LineNumberTable", e);
            }
        }
        return new LineNumberTable(name, types);
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + lineNumbers.size() * LineNumberDesc.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, lineNumbers.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < lineNumbers.size(); i++) {
            numBytes += lineNumbers.get(i).writeTo(out);
        }
        return numBytes;
    }


    public static class LineNumberDesc {

        public final int startPC;
        public final int lineNumber;

        public LineNumberDesc(int startPC, int lineNumber) {
            this.startPC = startPC;
            this.lineNumber = lineNumber;
        }

        public boolean isValid() {
            return true;
        }

        public static LineNumberDesc readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "startPC";
                int startPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "lineNumber";
                int lineNumber = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                return new LineNumberDesc(startPC, lineNumber);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of LineNumberDesc");
            }
        }

        public static int getSize() {
            return SIZE_SHORT + SIZE_SHORT;
        }

        public int writeTo(WritableByteChannel out) throws IOException {
            int numBytes = 0;
            putUnsignedShort(out, startPC, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            putUnsignedShort(out, lineNumber, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            return numBytes;
        }

    }

}
