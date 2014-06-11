package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public class Annotation {

    public final ConstantRef<StringConstant> type;
    public final ArrayList<ElementValuePair> elementValuePairs;

    public Annotation(ConstantRef<StringConstant> type, ArrayList<ElementValuePair> elementValuePairs) {
        this.type = type;
        this.elementValuePairs = elementValuePairs;
    }

    public boolean isValid() {
        return type.isValid(Constant.ConstType.STRING);
    }

    public static Annotation readFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
        if (length < ConstantRef.getSize() + SIZE_SHORT) throw new ReadException("Provided length of annotation is incorrect, should be at least " + (ConstantRef.getSize() + SIZE_SHORT) + " but was " + length);
        String part = "";
        try {
            part = "type";
            ConstantRef<StringConstant> type = ConstantRef.readFrom(pool, in);
            length -= ConstantRef.getSize();
            part = "numElemValuePairs";
            int numElemValuePairs = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            length -= SIZE_SHORT;
            ArrayList<ElementValuePair> elementValuePairs = new ArrayList<ElementValuePair>(numElemValuePairs);
            for (int j = 0; j < numElemValuePairs; j++) {
                part = "ElementValuePair_" + j;
                ElementValuePair elementValuePair = ElementValuePair.readFrom(pool, in, length);
                length -= elementValuePair.getSize();
                elementValuePairs.add(elementValuePair);
            }
            return new Annotation(type, elementValuePairs);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " of Annotation", e);
        }
    }

    public int getSize() {
        int size = ConstantRef.getSize() + SIZE_SHORT;
        for (ElementValuePair pair : elementValuePairs) {
            size += pair.getSize();
        }
        return size;
    }

    public int writeTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        numBytes += type.writeTo(out);
        putUnsignedShort(out, elementValuePairs.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < elementValuePairs.size(); i++) {
            numBytes += elementValuePairs.get(i).writeTo(out);
        }
        return  numBytes;
    }
}
