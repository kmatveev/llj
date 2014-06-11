package llj.packager.jclass.attributes;

import llj.packager.jclass.WithAttributes;
import llj.packager.jclass.constants.ClassRefConstant;
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

import static llj.util.BinIOTools.SIZE_INT;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getBytes;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putBytes;
import static llj.util.BinIOTools.putUnsignedChar;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class Code extends Attribute implements WithAttributes {

    public static final AttributeType TYPE = AttributeType.CODE;
    public int maxStack, maxLocals;
    public byte[] code;
    public final List<ExceptionTableEntry> exceptionTable;
    public final List<Attribute> attributes;

    public Code(ConstantRef<StringConstant> name, byte[] code, int maxStack, int maxLocals, List<ExceptionTableEntry> exceptionTable, List<Attribute> attributes) {
        super(name);
        this.code = code;
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.exceptionTable = exceptionTable;
        this.attributes = attributes;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public static Code readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        int minLength = SIZE_SHORT + SIZE_SHORT + SIZE_INT;
        if (length < minLength) throw new ReadException("Incorrect specified attribute length; must be at least: " + minLength + "; specified: " + length);
        String part = "";
        try {
            part = "maxStack";
            int maxStack = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            part = "maxLocals";
            int maxLocals = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            part = "codeLen";
            int codeLen = getInt(in, ByteOrder.BIG_ENDIAN); // getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            part = "code";
            if (codeLen + SIZE_INT + SIZE_SHORT + SIZE_SHORT > length) throw new ReadException("Incorrect specified code length, beyond the boundary of attribute");
            byte[] code = getBytes(in, codeLen);
            part = "exceptionTableLen";
            if (codeLen + SIZE_INT + SIZE_SHORT + SIZE_SHORT + SIZE_SHORT > length) throw new ReadException("No exceptionTableLen");
            int exceptionTableLen = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            part = "exceptionTable";
            if (codeLen + SIZE_INT + SIZE_SHORT + SIZE_SHORT + SIZE_SHORT + exceptionTableLen*ExceptionTableEntry.getSize() > length) throw new ReadException("Incorrect number of exception table entry, beyond the boundary of attribute");
            List<ExceptionTableEntry> exceptionTable = new ArrayList<ExceptionTableEntry>(exceptionTableLen);
            for (int j = 0; j < exceptionTableLen; j++) {
                part = "ExceptionTableEntry_" + j;
                ExceptionTableEntry entry = ExceptionTableEntry.readFrom(pool, in);
                exceptionTable.add(entry);
            }
            part = "attributes";
            int remaining = length - (codeLen + SIZE_INT + SIZE_SHORT + SIZE_SHORT + SIZE_SHORT + exceptionTableLen*ExceptionTableEntry.getSize());
            List<Attribute> attributes = Attribute.readList(pool, in, remaining);
            return new Code(name, code, maxStack, maxLocals, exceptionTable, attributes);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " of Code", e);
        }
    }

    @Override
    public int getValueSize() {
        int size = SIZE_SHORT + SIZE_SHORT + SIZE_INT + code.length;
        size += exceptionTable.size() * ExceptionTableEntry.getSize();
        size += Attribute.getTotalSize(attributes);
        return size;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, maxStack, ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        putUnsignedShort(out, maxLocals, ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        putUnsignedInt(out, code.length, ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_INT;
        putBytes(out, code);
        numBytes += code.length;
        putUnsignedShort(out, exceptionTable.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < exceptionTable.size(); i++) {
            numBytes += exceptionTable.get(i).writeTo(out);
        }
        numBytes += Attribute.writeList(out, attributes);
        return numBytes;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        return result;
    }

    public static class ExceptionTableEntry {

        public final int startPC, endPC, handlerPC;
        public final ConstantRef<ClassRefConstant> catchClassRef;

        public ExceptionTableEntry(int startPC, int endPC, int handlerPC, ConstantRef<ClassRefConstant> catchClassRef) {
            this.startPC = startPC;
            this.endPC = endPC;
            this.handlerPC = handlerPC;
            this.catchClassRef = catchClassRef;
        }

        public static ExceptionTableEntry readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "startPC";
                int startPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "endPC";
                int endPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "handlerPC";
                int handlerPC = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                part = "catchClassRef";
                ConstantRef<ClassRefConstant> catchClassRef = ConstantRef.readFrom(pool, in);
                return new ExceptionTableEntry(startPC, endPC, handlerPC, catchClassRef);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of ExceptionTableEntry", e);
            }
        }

        public static int getSize() {
            return SIZE_SHORT + SIZE_SHORT + SIZE_SHORT + ConstantRef.getSize();
        }

        public int writeTo(WritableByteChannel out) throws IOException {
            int numBytes = 0;
            putUnsignedShort(out, startPC, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            putUnsignedShort(out, endPC, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            putUnsignedShort(out, handlerPC, ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            numBytes += catchClassRef.writeTo(out);
            return numBytes;
        }
    }
}
