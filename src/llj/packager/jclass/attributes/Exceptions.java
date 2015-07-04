package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ClassRefConstant;
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
import java.util.List;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public class Exceptions extends Attribute {

    public static final AttributeType TYPE = AttributeType.EXCEPTIONS;

    public final ArrayList<ConstantRef<ClassRefConstant> > exceptions;

    public Exceptions(ConstantRef<StringConstant> name, ArrayList<ConstantRef<ClassRefConstant> > exceptions) {
        super(name);
        this.exceptions = exceptions;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < exceptions.size(); i++) {
            ConstantRef<ClassRefConstant> ref = exceptions.get(i);
            if (! ref.isValid(Constant.ConstType.CLASS_REF)) {
                errors.add("ExceptionEntry_" + i + " is not a valid reference to ClassRefConstant");
                result = false;
            }
        }
        return result;
    }

    public static Exceptions readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in Exceptions");
        }
        int expectedLength = SIZE_SHORT + numOf * ConstantRef.getSize();
        if (length != expectedLength) throw new ReadException("Incorrect specified attribute length; for " + numOf + " InnerClass entries; it should be " + expectedLength + "; specified: " + length);

        ArrayList<ConstantRef<ClassRefConstant> > exceptions = new ArrayList<ConstantRef<ClassRefConstant> >();
        for (int j = 0; j < numOf; j++) {
            try {
                ConstantRef<ClassRefConstant> exceptionClassRef = ConstantRef.readFrom(pool, in);
                exceptions.add(exceptionClassRef);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read ExceptionEntry_" + j + " of Exceptions", e);
            }
        }
        return new Exceptions(name, exceptions);
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + exceptions.size() * ConstantRef.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, exceptions.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < exceptions.size(); i++) {
            numBytes += exceptions.get(i).writeTo(out);
        }
        return numBytes;
    }
}
