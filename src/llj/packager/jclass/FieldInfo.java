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

public class FieldInfo extends ClassMemberInfo {

    public static final EnumSet<AccessFlags> FIELD_ACCESS_FLAGS = EnumSet.noneOf(AccessFlags.class);

    static {
        FieldInfo.FIELD_ACCESS_FLAGS.addAll(Arrays.asList(new AccessFlags[]{
                AccessFlags.PUBLIC, AccessFlags.PRIVATE, AccessFlags.PROTECTED,
                AccessFlags.STATIC, AccessFlags.FINAL, AccessFlags.VOLATILE,
                AccessFlags.TRANSIENT, AccessFlags.SYNTHETIC, AccessFlags.ENUM}));

    }

    public FieldInfo(EnumSet<AccessFlags> accessFlags, ConstantRef<StringConstant> name, ConstantRef<StringConstant> descriptor, List<Attribute> attributes) {
        super(accessFlags, name, descriptor, attributes);
    }

    public static FieldInfo readFrom(ConstantPool pool, ReadableByteChannel in) throws ReadException {
        String part = "";
        try {
            part = "accessFlags";
            int accessFlagsRaw = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            EnumSet<AccessFlags> accessFlags = AccessFlags.maskFrom(FIELD_ACCESS_FLAGS, accessFlagsRaw);
            part = "nameRef";
            ConstantRef<StringConstant> name = ConstantRef.readFrom(pool, in);
            part = "descriptorRef";
            ConstantRef<StringConstant> descriptor = ConstantRef.readFrom(pool, in);
            part = "attributes";
            List<Attribute> attributes =  Attribute.readList(pool, in, -1);
            return new FieldInfo(accessFlags, name, descriptor, attributes);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of FieldInfo", e);
        }
    }

    public int getSize() {
        return SIZE_SHORT + ConstantRef.getSize() + ConstantRef.getSize() + Attribute.getTotalSize(attributes);
    }

}
