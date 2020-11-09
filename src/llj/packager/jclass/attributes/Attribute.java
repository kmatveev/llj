package llj.packager.jclass.attributes;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.ResolveException;
import llj.packager.jclass.constants.StringConstant;
import llj.util.BinIOTools;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static llj.util.BinIOTools.SIZE_INT;
import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putInt;
import static llj.util.BinIOTools.putUnsignedShort;

public abstract class Attribute {

    public static enum AttributeType {

        UNKNOWN(null) {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return UnknownAttribute.readFrom(pool, name, in, length);
            }
        },
        CODE("Code") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return Code.readFrom(pool, name, in, length);
            }
        },
        CONSTANT_VALUE("ConstantValue") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return ConstantValue.readFrom(pool, name, in, length);
            }
        },
        ENCLOSING_METHOD("EnclosingMethod") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return EnclosingMethod.readFrom(pool, name, in, length);
            }
        },
        EXCEPTIONS("Exceptions") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return Exceptions.readFrom(pool, name, in, length);
            }
        },
        INNER_CLASSES("InnerClasses") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return InnerClasses.readFrom(pool, name, in, length);
            }
        },
        SIGNATURE("Signature") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return Signature.readFrom(pool, name, in, length);
            }
        },
        SOURCE_FILE("SourceFile") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return SourceFile.readFrom(pool, name, in, length);
            }
        },
        SYNTHETIC("Synthetic") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return Synthetic.readFrom(pool, name, in, length);
            }
        },
        ANNOT_DEFAULT("AnnotationDefault"){
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return AnnotationDefault.readFrom(pool, name, in, length);
            }
        },
        LINE_NUMBER_TABLE("LineNumberTable") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return LineNumberTable.readFrom(pool, name, in, length);
            }
        },
        LOCAL_VAR_TABLE("LocalVariableTable") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return LocalVariableTable.readFrom(pool, name, in, length);
            }
        },
        LOCAL_VAR_TYPE_TABLE("LocalVariableTypeTable") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return LocalVariableTypeTable.readFrom(pool, name, in, length);
            }
        },
        RT_VIS_ANNOTATIONS("RuntimeVisibleAnnotations") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return RuntimeVisibleAnnotations.readFrom(pool, name, in, length);
            }
        },
        RT_INV_ANNOTATIONS("RuntimeInvisibleAnnotations") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return RuntimeInvisibleAnnotations.readFrom(pool, name, in, length);
            }
        },
        STACK_MAP_TABLE("StackMapTable") {
            public Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
                return StackMapTable.readFrom(pool, name, in, length);
            }
        }
        ;

        public final String name;

        private AttributeType(String name) {
            this.name = name;
        }

        public abstract Attribute readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException;

        public static AttributeType getByName(String name) {
            for (Attribute.AttributeType type : values()) {
                if (type.name != null && type.name.equals(name)) return type;
            }
            return UNKNOWN;
        }

    }

    public static List<Attribute> readList(ConstantPool pool, ReadableByteChannel in, int totalLength) throws ReadException {
        boolean checkLength = totalLength >= 0;
        if (checkLength && (totalLength < SIZE_SHORT)) throw new ReadException("Was unable to read a number of attributes in a list, reading beyond specified limit");
        int numOfAttributes = getUnsignedShort(in, ByteOrder.BIG_ENDIAN);
        totalLength -= SIZE_SHORT;
        List<Attribute> attributes = new ArrayList<Attribute>(numOfAttributes);
        for (int i = 0; i < numOfAttributes; i++) {
            String part = "";
            try {

                part = "nameRef";
                if (checkLength && (totalLength < ConstantRef.getSize())) throw new ReadException("reading beyond specified limit");
                ConstantRef<StringConstant> nameRef = ConstantRef.readFrom(pool, in);
                totalLength -= ConstantRef.getSize();
                String name;
                try {
                    name = nameRef.resolve().value;
                } catch (ResolveException e) {
                    throw new ReadException("Was unable to resolve attribute name from constant pool", e);
                }

                part = "length";
                if (checkLength && (totalLength < SIZE_INT)) throw new ReadException("reading beyond specified limit");
                int length = getInt(in, ByteOrder.BIG_ENDIAN);
                totalLength -= SIZE_INT;

                part = "value";
                if (checkLength && (length > totalLength)) throw new ReadException("Specified attribute length " + length + " is larger that provided limit " + totalLength);
                Attribute attrib = AttributeType.getByName(name).readFrom(pool, nameRef, in, length);
                totalLength -= attrib.getValueSize();
                attributes.add(attrib);
            } catch (ReadException e) {
                throw new ReadException("Was unable to read a " + part + " part of AttributeListEntry_" + i, e);
            }
        }
        return attributes;
    }

    public static int getTotalSize(List<Attribute> attributes) {
        int size = SIZE_SHORT;
        for (Attribute attribute : attributes) {
            size += attribute.getLength();
        }
        return size;
    }

    public static int writeList(WritableByteChannel out, List<Attribute> list) throws IOException {
        putUnsignedShort(out, list.size(), ByteOrder.BIG_ENDIAN);
        int bytesConsumed = SIZE_SHORT;
        for (int i = 0; i < list.size(); i++) {
            Attribute attrib = list.get(i);
            bytesConsumed += attrib.writeTo(out);
        }
        return bytesConsumed;
    }


    public final ConstantRef<StringConstant> name;

    public abstract AttributeType getType();

    public abstract int getValueSize();

    public Attribute(ConstantRef<StringConstant> name) {
        this.name = name;
    }

    public int getLength() {
        return ConstantRef.getSize() + SIZE_INT + getValueSize();
    }

    public int writeTo(WritableByteChannel out) throws IOException {
        int bytesConsumed = 0;
        bytesConsumed += name.writeTo(out);
        putInt(out, getLength(), ByteOrder.BIG_ENDIAN);
        bytesConsumed += BinIOTools.SIZE_INT;
        bytesConsumed += writeValueTo(out);
        return bytesConsumed;
    }

    public abstract int writeValueTo(WritableByteChannel out) throws IOException;

    public boolean validate(List<String> errors) {
        boolean result = true;
        if (!name.isValid(Constant.ConstType.STRING)) {
            errors.add("Attribute name is invalid");
            result = false;
        }
        return result;
    }

    public String resolveName() throws FormatException {
        return name.resolve().value;
    }

}
