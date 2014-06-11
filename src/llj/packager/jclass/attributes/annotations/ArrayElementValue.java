package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.ConstantPool;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import static llj.util.BinIOTools.SIZE_CHAR;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public class ArrayElementValue extends ElementValue {

    public final ArrayList<ElementValue> values;

    public ArrayElementValue(ArrayList<ElementValue> values) {
        this.values = values;
    }

    @Override
    public ElementType getType() {
        return ElementType.ARRAY;
    }

    public static ArrayElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Was unable to read numOfElements of ArrayElementValue, it is beyond specified limit");
        int numOfElements = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        length -= SIZE_SHORT;
        ArrayList<ElementValue> values = new ArrayList<ElementValue>(numOfElements);
        for (int j = 0; j < numOfElements; j++) {
            String part = "";
            try {
                part = "tag";
                char c = BinIOTools.getUnsignedChar(in);
                length -= SIZE_CHAR;
                part = "value";
                ElementValue elemValue = ElementType.readFrom(c, pool, in, length);
                length -= elemValue.getSize();
                values.add(elemValue);
            } catch (ReadException e) {
                throw new RuntimeException("Was unable to read " + part + " part of ArrayElementValueEntry_" + j, e);
            }
        }
        return new ArrayElementValue(values);
    }

    @Override
    public int getSize() {
        int size = SIZE_SHORT;
        for (ElementValue value : values) {
            size += SIZE_CHAR;
            size += value.getSize();
        }
        return size;
    }

    @Override
    public int writeTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, values.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < values.size(); i++) {
            numBytes += values.get(i).writeTo(out);
        }
        return numBytes;
    }
}
