package llj.packager.jclass;

import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.util.EnumSet;
import java.util.List;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedShort;

public abstract class ClassMemberInfo implements WithAttributes {

    public final EnumSet<AccessFlags> accessFlags;
    public final ConstantRef<StringConstant> name;
    public final ConstantRef<StringConstant> descriptor;
    public final List<Attribute> attributes;

    protected ClassMemberInfo(EnumSet<AccessFlags> accessFlags, ConstantRef<StringConstant> name, ConstantRef<StringConstant> descriptor, List<Attribute> attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public String resolveName() throws FormatException {
        return name.resolve().value;
    }

    public String resolveDescriptor() throws FormatException {
        return descriptor.resolve().value;
    }

    public int writeTo(WritableByteChannel out) throws IOException, FormatException {
        putUnsignedShort(out, AccessFlags.pack(accessFlags), ByteOrder.BIG_ENDIAN);
        int bytesConsumed = SIZE_SHORT;
        bytesConsumed += name.writeTo(out);
        bytesConsumed += descriptor.writeTo(out);
        bytesConsumed += Attribute.writeList(out, attributes);
        return bytesConsumed;
    }

    public abstract int getSize();

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public boolean validate(List<String> errors) {
        boolean result = true;
        if (!name.isValid(Constant.ConstType.STRING)) {
            result = false;
            errors.add("nameRef is not a valid ConstantRef");
        }
        if (!descriptor.isValid(Constant.ConstType.STRING)) {
            result = false;
            errors.add("descriptorRef is not a valid ConstantRef");
        }
        for (Attribute attr : attributes) {
            result &= attr.validate(errors);
        }
        return result;
    }

    public boolean matches(String name, String descriptor) throws FormatException {
        return resolveName().equals(name) && resolveDescriptor().equals(descriptor);
    }
}
