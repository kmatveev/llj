package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.ConstantPool;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class ElementValue {

    public static enum ElementType {

        CONST(new char[] {'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's'}) {
            @Override
            public ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
                return ConstIndexElementValue.readFrom(pool, tag, in, length);
            }
        },
        ENUM_CONST('e') {
            @Override
            public ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
                return EnumConstElementValue.readFrom(pool, tag, in, length);
            }
        },
        CLASS_INFO('c') {
            @Override
            public ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
                return ClassInfoIndexElementValue.readFrom(pool, tag, in, length);
            }
        },
        ANNOTATION('@') {
            @Override
            public ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
                return AnnotationElementValue.readFrom(pool, tag, in, length);
            }
        },
        ARRAY('[') {
            @Override
            public ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
                return ArrayElementValue.readFrom(pool, tag, in, length);
            }
        };


        private ElementType(char tag) {
            this.tags = new char[] {tag};
        }

        private ElementType(char[] tags) {
            this.tags = tags;
        }

        public final char[] tags;

        protected abstract ElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException;

        public static ElementValue readFrom(char tag, ConstantPool pool, ReadableByteChannel in, int length) throws ReadException {
            for (ElementType type : ElementType.values()) {
                for (char t : type.tags) {
                    if (t == tag) return type.readFrom(pool, tag, in, length);
                }
            }
            throw new IllegalArgumentException("Bad tag:" + tag);
        }

    }

    public abstract ElementType getType();

    public abstract int getSize();

    public char getTag() {
        ElementType type = getType();
        if (type.tags.length == 1) {
            return  type.tags[0];
        } else {
            throw new UnsupportedOperationException("No default tag");
        }
    }

    public abstract int writeTo(WritableByteChannel out) throws IOException;

}
