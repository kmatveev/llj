package llj.packager.jclass;

import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.constants.ClassRefConstant;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static llj.util.BinIOTools.*;

public class ClassFileFormat implements WithAttributes {

    public static final EnumSet<AccessFlags> CLASS_ACCESS_FLAGS = EnumSet.noneOf(AccessFlags.class);

    static {

        CLASS_ACCESS_FLAGS.addAll(Arrays.asList(new AccessFlags[]{
                AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.SUPER, AccessFlags.INTERFACE,
                AccessFlags.ABSTRACT, AccessFlags.SYNTHETIC, AccessFlags.ANNOTATION, AccessFlags.ENUM}));

    }


    public final byte[] magic = new byte[] {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
    public final int minorVersion;
    public final int majorVersion;
    public final ConstantPool constPool;
    public final EnumSet<AccessFlags> accessFlags;
    public final ConstantRef<ClassRefConstant> thisClassRef;
    public final ConstantRef<ClassRefConstant> parentClassRef;
    public final List< ConstantRef<ClassRefConstant> > interfaces;
    public final List<FieldInfo> fields;
    public final List<MethodInfo> methods;
    public final List<Attribute> attributes;

    public ClassFileFormat(int minorVersion,
                           int majorVersion,
                           ConstantPool constPool,
                           EnumSet<AccessFlags> accessFlags,
                           ConstantRef<ClassRefConstant> thisClassRef,
                           ConstantRef<ClassRefConstant> parentClassRef,
                           ArrayList<ConstantRef<ClassRefConstant>> interfaces,
                           List<FieldInfo> fields,
                           List<MethodInfo> methods,
                           List<Attribute> attributes) {
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constPool = constPool;
        this.accessFlags = accessFlags;
        this.thisClassRef = thisClassRef;
        this.parentClassRef = parentClassRef;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
    }

    public static ClassFileFormat readFrom(ReadableByteChannel in) throws ReadException {

        String part = "";
        try {
            part = "header";
            ByteBuffer readBuffer = ByteBuffer.allocate(8);
            readBuffer.order(ByteOrder.BIG_ENDIAN);
            BinIOTools.readFully(in, readBuffer);
            readBuffer.flip();

            byte[] magic = new byte[4];
            readBuffer.get(magic);

            int minorVersion = getUnsignedShort(readBuffer);
            int majorVersion = getUnsignedShort(readBuffer);

            part = "ConstantPool";
            ConstantPool constPool = new ConstantPool();
            constPool.readFrom(in);

            part = "AccessFlags";
            int accessFlagsRaw = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            EnumSet<AccessFlags> accessFlags = AccessFlags.maskFrom(CLASS_ACCESS_FLAGS, accessFlagsRaw);

            part = "thisClassRef";
            ConstantRef<ClassRefConstant> thisClassRef = ConstantRef.readFrom(constPool, in);
            part = "parentClassRef";
            ConstantRef<ClassRefConstant> parentClassRef = ConstantRef.readFrom(constPool, in);

            part = "numOfInterfaces";
            int numOfInterfaces = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            ArrayList< ConstantRef<ClassRefConstant> > interfaces = new ArrayList< ConstantRef<ClassRefConstant> >(numOfInterfaces);
            int interfacesSize = SIZE_SHORT;
            for (int i = 0; i < numOfInterfaces; i++) {
                part = "InterfaceEntry_" + i;
                ConstantRef<ClassRefConstant> ref = ConstantRef.readFrom(constPool, in);
                interfaces.add(ref);
            }

            part = "numOfFields";
            int numOfFields = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            ArrayList<FieldInfo> fields = new ArrayList<FieldInfo> (numOfFields);
            int fieldsSize = SIZE_SHORT;
            for (int i = 0; i < numOfFields; i++) {
                part = "FieldInfo_" + i;
                FieldInfo fieldDesc = FieldInfo.readFrom(constPool, in);
                fields.add(fieldDesc);
            }

            part = "numOfMethods";
            int numOfMethods = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
            ArrayList<MethodInfo> methods = new ArrayList<MethodInfo> (numOfMethods);
            int methodsSize = SIZE_SHORT;
            for (int i = 0; i < numOfMethods; i++) {
                part = "MethodInfo_" + i;
                MethodInfo methodDesc = MethodInfo.readFrom(constPool, in);
                methods.add(methodDesc);
            }

            List<Attribute> attributes = Attribute.readList(constPool, in, -1);

            return new ClassFileFormat(minorVersion, majorVersion, constPool, accessFlags, thisClassRef, parentClassRef, interfaces, fields, methods, attributes);

//            Map<String, Integer> statistics = new HashMap<String, Integer>();
//            statistics.put("Header", headerSize);
//            statistics.put("Constants", constPoolSize);
//            statistics.put("Interfaces", interfacesSize);
//            statistics.put("Fields", fieldsSize);
//            statistics.put("Methods", methodsSize);
//            statistics.put("Attributes", attributesSize);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read " + part + " part of class file", e);
        }
    }

    public void writeTo(WritableByteChannel out) throws Exception {
        writeTo(out, new HashMap<String, Integer>());
    }

    public void writeTo(WritableByteChannel out, Map<String, Integer> statistics) throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(8);
        writeBuffer.order(ByteOrder.BIG_ENDIAN);

        writeBuffer.put(magic);
        putUnsignedShort(writeBuffer, minorVersion);
        putUnsignedShort(writeBuffer, majorVersion);

        writeBuffer.flip();
        out.write(writeBuffer);

        int headerSize = 8;

        int constPoolSize = constPool.writeTo(out);

        putUnsignedShort(out, AccessFlags.pack(accessFlags), ByteOrder.BIG_ENDIAN);

        thisClassRef.writeTo(out);
        parentClassRef.writeTo(out);

        putUnsignedShort(out, interfaces.size(), ByteOrder.BIG_ENDIAN);
        int interfacesSize = SIZE_SHORT;
        for (int i = 0; i < interfaces.size(); i++) {
            interfacesSize += interfaces.get(i).writeTo(out);
        }

        putUnsignedShort(out, fields.size(), ByteOrder.BIG_ENDIAN);
        int fieldsSize = SIZE_SHORT;
        for (int i = 0; i < fields.size(); i++) {
            fieldsSize += fields.get(i).writeTo(out);
        }

        putUnsignedShort(out, methods.size(), ByteOrder.BIG_ENDIAN);
        int methodsSize = SIZE_SHORT;
        for (int i = 0; i < methods.size(); i++) {
            methodsSize += methods.get(i).writeTo(out);
        }

        int attributesSize = Attribute.writeList(out, attributes);

        statistics.put("Header", headerSize);
        statistics.put("Constants", constPoolSize);
        statistics.put("Interfaces", interfacesSize);
        statistics.put("Fields", fieldsSize);
        statistics.put("Methods", methodsSize);
        statistics.put("Attributes", attributesSize);
    }


    public List<String> validate() {

        ArrayList<String> errors = new ArrayList<String>();

        if (!thisClassRef.isValid(Constant.ConstType.CLASS_REF)) {
            errors.add("Reference to this class is invalid");
        }

        if (!parentClassRef.isValid(Constant.ConstType.CLASS_REF)) {
            errors.add("Reference to parent class is invalid");
        }

        for (ConstantRef<ClassRefConstant> interfaceRef : interfaces) {
            if (!interfaceRef.isValid(Constant.ConstType.CLASS_REF))
            errors.add("Reference to interface is invalid");
        }

        for (FieldInfo field : fields) {
            field.validate(errors);
        }

        for (MethodInfo method : methods) {
            method.validate(errors);
        }

        for (Attribute attrib : attributes) {
            attrib.validate(errors);
        }

        return errors;
    }


    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }
}