package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.attributes.StackMapTable;
import llj.packager.jclass.constants.ResolveException;
import llj.util.CharTools;

import java.io.IOException;
import java.nio.CharBuffer;

public abstract class Type {

    public final TypeType type;

    Type(TypeType type) {
        this.type = type;
    }

    public static Type fromFormat(String format) {
        CharBuffer buffer = CharBuffer.wrap(format);
        try {
            return fromFormat(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Type fromFormat(CharBuffer format) throws IOException {
        char id = format.get();
        TypeType type = TypeType.parse(id);
        if (type == TypeType.REF) {
            String className = CharTools.readUntil(format, ';', false);
            format.get();
            ClassReference reference = ClassReference.makeClassRef(className);
            return RefType.instanceRef(reference);
        } else if (type == TypeType.ARRAY_REF) {
            return new ArrayRefType(fromFormat(format));
        } else {
            return ScalarType.scalar(type);
        }
    }

    public abstract String toCode();

    public String toString() {
        return toCode();
    }

    public abstract boolean isAssignableFrom(Type another) throws ClassesNotLoadedException;

    public static Type fromVerificationTypeInfo(StackMapTable.VerificationTypeInfo info) throws ResolveException {
        switch (info.getKind()) {
            case TOP_VARIABLE_INFO:
                return TopType.instance;
            case INTEGER_VARIABLE_INFO:
                return ScalarType.scalar(TypeType.INT);
            case FLOAT_VARIABLE_INFO:
                return ScalarType.scalar(TypeType.FLOAT);
            case LONG_VARIABLE_INFO:
                return ScalarType.scalar(TypeType.LONG);
            case DOUBLE_VARIABLE_INFO:
                return ScalarType.scalar(TypeType.DOUBLE);
            case NULL_VARIABLE_INFO:
                return RefType.anyRef();
            case UNINITIALIZED_THIS_VARIABLE_INFO:
                return RefType.anyRef();
            case OBJECT_VARIABLE_INFO:
                StackMapTable.ObjectVariableInfo refInfo = (StackMapTable.ObjectVariableInfo)info;
                ClassReference classRef = ClassReference.make(refInfo.classRef.resolve());
                return RefType.instanceRef(classRef);
            case UNINITIALIZED_VARIABLE_INFO:
                return RefType.anyRef();
            default:
                throw new RuntimeException();
        }
    }
}
