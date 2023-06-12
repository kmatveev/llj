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

public abstract class Annotation extends Attribute {

    public final ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations;

    protected Annotation(ConstantRef<StringConstant> name, ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations) {
        super(name);
        this.annotations = annotations;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < annotations.size(); i++) {
            llj.packager.jclass.attributes.annotations.Annotation ref = annotations.get(i);
            if (! ref.isValid()) {
                result = false;
                errors.add("Annotation " + i + " is invalid");
            }
        }
        return result;
    }

    public static ArrayList<llj.packager.jclass.attributes.annotations.Annotation> readAnnotationsFrom(ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in LocalVariableTable");
        }
        length -= SIZE_SHORT;

        ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations = new ArrayList<llj.packager.jclass.attributes.annotations.Annotation>(numOf);
        for (int j = 0; j < numOf; j++) {
            try {
                llj.packager.jclass.attributes.annotations.Annotation annotation = llj.packager.jclass.attributes.annotations.Annotation.readFrom(pool, in, length);
                length -= annotation.getSize();
                annotations.add(annotation);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read LineNumberEntry_" + j + " of LineNumberTable", e);
            }
        }
        return annotations;
    }

    public int getValueSize() {
        int size = SIZE_SHORT;
        for (llj.packager.jclass.attributes.annotations.Annotation annotation : annotations) {
            size += annotation.getSize();
        }
        return size;
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, annotations.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < annotations.size(); i++) {
            numBytes += annotations.get(i).writeTo(out);
        }
        return numBytes;
    }
}
