package llj.packager.jclass.attributes;

import llj.packager.jclass.AccessFlags;
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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public class InnerClasses extends Attribute {

    public static final AttributeType TYPE = AttributeType.INNER_CLASSES;

    public static final EnumSet<AccessFlags> INNER_CLASS_ACCESS_FLAGS = EnumSet.noneOf(AccessFlags.class);

    static {
        InnerClasses.INNER_CLASS_ACCESS_FLAGS.addAll(Arrays.asList(new AccessFlags[]{
                AccessFlags.PUBLIC, AccessFlags.PRIVATE, AccessFlags.PROTECTED,
                AccessFlags.STATIC, AccessFlags.FINAL, AccessFlags.INTERFACE,
                AccessFlags.ABSTRACT, AccessFlags.SYNTHETIC, AccessFlags.ANNOTATION, AccessFlags.ENUM}));

    }

    public final ArrayList<InnerClassDesc> innerClasses;

    public InnerClasses(ConstantRef<StringConstant> name, ArrayList<InnerClassDesc> innerClasses) {
        super(name);
        this.innerClasses = innerClasses;
    }

    public AttributeType getType() {
        return TYPE;
    }

    @Override
    public boolean validate(List<String> errors) {
        boolean result = super.validate(errors);
        for (int i = 0; i < innerClasses.size(); i++) {
            InnerClassDesc innerClassDesc = innerClasses.get(i);
            if (! innerClassDesc.isValid()) {
                errors.add("InnerClassDesc_" + i + " is not valid");
                result = false;
            }
        }
        return result;
    }

    public static InnerClasses readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        if (length < SIZE_SHORT) throw new ReadException("Incorrect specified attribute length; must at least: " + SIZE_SHORT + "; specified: " + length);
        int numOf = 0;
        try {
            numOf = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read number of entries in InnerClasses");
        }
        int expectedLength = SIZE_SHORT + numOf * InnerClassDesc.getSize();
        if (length != expectedLength) throw new ReadException("Incorrect specified attribute length; for " + numOf + " InnerClass entries; it should be " + expectedLength + "; specified: " + length);

        ArrayList<InnerClassDesc> innerClasses = new ArrayList<InnerClassDesc>();
        for (int j = 0; j < numOf; j++) {
            try {
                InnerClassDesc innerClass = InnerClassDesc.readFrom(pool, in);
                innerClasses.add(innerClass);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read LineNumberEntry_" + j + " of LineNumberTable", e);
            }
        }
        return new InnerClasses(name, innerClasses);
    }

    @Override
    public int getValueSize() {
        return SIZE_SHORT + innerClasses.size() * InnerClassDesc.getSize();
    }

    @Override
    public int writeValueTo(WritableByteChannel out) throws IOException {
        int numBytes = 0;
        putUnsignedShort(out, innerClasses.size(), ByteOrder.BIG_ENDIAN);
        numBytes += SIZE_SHORT;
        for (int i = 0; i < innerClasses.size(); i++) {
            numBytes += innerClasses.get(i).writeTo(out);
        }
        return numBytes;
    }

    public static class InnerClassDesc {

        public final ConstantRef<ClassRefConstant> innerClassRef, outerClassRef;
        public final ConstantRef<StringConstant> innerNameRef;
        public final EnumSet<AccessFlags> accessFlags;

        public InnerClassDesc(ConstantRef<ClassRefConstant> innerClassRef, ConstantRef<ClassRefConstant> outerClassRef, ConstantRef<StringConstant> innerNameRef, EnumSet<AccessFlags> accessFlags) {
            this.innerClassRef = innerClassRef;
            this.outerClassRef = outerClassRef;
            this.innerNameRef = innerNameRef;
            this.accessFlags = accessFlags;
        }

        public boolean isValid() {
            return innerClassRef.isValid(Constant.ConstType.CLASS_REF);
        }

        public static InnerClassDesc readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
            String part = "";
            try {
                part = "innerClassRef";
                ConstantRef<ClassRefConstant> innerClassRef = ConstantRef.readFrom(pool, in);
                part = "outerClassRef";
                ConstantRef<ClassRefConstant> outerClassRef  = ConstantRef.readFrom(pool, in);
                part = "innerNameRef";
                ConstantRef<StringConstant> innerNameRef = ConstantRef.readFrom(pool, in);
                part = "accessFlags";
                int accessFlagsRaw = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
                EnumSet<AccessFlags> accessFlags = AccessFlags.maskFrom(INNER_CLASS_ACCESS_FLAGS, accessFlagsRaw);
                return new InnerClassDesc(innerClassRef, outerClassRef, innerNameRef, accessFlags);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read " + part + " part of InnerClassDesc", e);
            }
        }

        public static int getSize() {
            return ConstantRef.getSize() + ConstantRef.getSize() + ConstantRef.getSize() + SIZE_SHORT;
        }

        public int writeTo(WritableByteChannel out) throws IOException {
            int numBytes = 0;
            numBytes += innerClassRef.writeTo(out);
            numBytes += outerClassRef.writeTo(out);
            numBytes += innerNameRef.writeTo(out);
            putUnsignedShort(out, AccessFlags.pack(accessFlags), ByteOrder.BIG_ENDIAN);
            numBytes += SIZE_SHORT;
            return numBytes;
        }
    }
}
