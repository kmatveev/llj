package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class RuntimeInvisibleAnnotations extends Annotation {

    public static final Attribute.AttributeType TYPE = Attribute.AttributeType.RT_VIS_ANNOTATIONS;

    public RuntimeInvisibleAnnotations(ConstantRef<StringConstant> name, ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations) {
        super(name, annotations);
    }

    public static RuntimeInvisibleAnnotations readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations = readAnnotationsFrom(pool, in, length);
        return new RuntimeInvisibleAnnotations(name, annotations);
    }

    @Override
    public Attribute.AttributeType getType() {
        return TYPE;
    }

}
