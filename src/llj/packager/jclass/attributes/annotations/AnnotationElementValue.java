package llj.packager.jclass.attributes.annotations;

import llj.packager.jclass.constants.ConstantPool;
import llj.util.ReadException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class AnnotationElementValue extends ElementValue {

    public final Annotation annotation;

    public AnnotationElementValue(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public ElementType getType() {
        return ElementType.ANNOTATION;
    }

    public static AnnotationElementValue readFrom(ConstantPool pool, char tag, ReadableByteChannel in, int length) throws ReadException {
        try {
            Annotation annotation = Annotation.readFrom(pool, in, length);
            return new AnnotationElementValue(annotation);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read annotation for AnnotationElementValue", e);
        }
    }

    @Override
    public int getSize() {
        return annotation.getSize();
    }

    public int writeTo(WritableByteChannel out) throws IOException {
        return annotation.writeTo(out);
    }
}
