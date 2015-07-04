package llj.packager.jclass;


import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;

public class MethodInfo extends ClassMemberInfo {

    public static final EnumSet<AccessFlags> METHOD_ACCESS_FLAGS = EnumSet.noneOf(AccessFlags.class);

    static {
        MethodInfo.METHOD_ACCESS_FLAGS.addAll(Arrays.asList(new AccessFlags[]{
                AccessFlags.PUBLIC, AccessFlags.PRIVATE, AccessFlags.PROTECTED,
                AccessFlags.STATIC, AccessFlags.FINAL, AccessFlags.SYNCHRONIZED,
                AccessFlags.BRIDGE, AccessFlags.VARARGS, AccessFlags.NATIVE,
                AccessFlags.ABSTRACT, AccessFlags.STRICT, AccessFlags.SYNTHETIC}));

    }

    public MethodInfo(EnumSet<AccessFlags> accessFlags, ConstantRef<StringConstant> name, ConstantRef<StringConstant> descriptor, List<Attribute> attributes) {
        super(accessFlags, name, descriptor, attributes);
    }

    public static MethodInfo readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
        String part = "";
        try {
            part = "accessFlags";
            int accessFlagsRaw = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            EnumSet<AccessFlags> accessFlags = AccessFlags.maskFrom(METHOD_ACCESS_FLAGS, accessFlagsRaw);
            part = "nameRef";
            ConstantRef<StringConstant> name = ConstantRef.readFrom(pool, in);
            part = "descriptorRef";
            ConstantRef<StringConstant> descriptor = ConstantRef.readFrom(pool, in);
            part = "attributes";
            List<Attribute> attributes =  Attribute.readList(pool, in, -1);
            return new MethodInfo(accessFlags, name, descriptor, attributes);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of MethodInfo", e);
        }
    }

    public int getSize() {
        return SIZE_SHORT + ConstantRef.getSize() + ConstantRef.getSize() + Attribute.getTotalSize(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodInfo) {
            return super.equals(obj);
        } else {
            return false;
        }
    }
}
