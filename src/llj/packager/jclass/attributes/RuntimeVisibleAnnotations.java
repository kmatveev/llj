package llj.packager.jclass.attributes;

import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.StringConstant;
import llj.util.ReadException;

import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class RuntimeVisibleAnnotations extends Annotation {

    public static final AttributeType TYPE = AttributeType.RT_VIS_ANNOTATIONS;

    public RuntimeVisibleAnnotations(ConstantRef<StringConstant> name, ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations) {
        super(name, annotations);
    }

    public static RuntimeVisibleAnnotations readFrom(ConstantPool pool, ConstantRef<StringConstant> name, ReadableByteChannel in, int length) throws ReadException {
        ArrayList<llj.packager.jclass.attributes.annotations.Annotation> annotations = readAnnotationsFrom(pool, in, length);
        return new RuntimeVisibleAnnotations(name, annotations);
    }

    @Override
    public AttributeType getType() {
        return TYPE;
    }

}
