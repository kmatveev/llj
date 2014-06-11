package llj.asm.bytecode;

import java.util.EnumMap;

public class ScalarType extends Type {

    private static final EnumMap<TypeType, ScalarType> scalars = new EnumMap<TypeType, ScalarType>(TypeType.class);

    private ScalarType(TypeType type) {
        super(type);
    }

    public static ScalarType scalar(TypeType type) {
        if (!type.isScalar) throw new IllegalArgumentException("This type is not scalar");
        if (!scalars.containsKey(type)) {
            scalars.put(type, new ScalarType(type));
        }
        return scalars.get(type);
    }

    @Override
    public boolean isAssignableFrom(Type another) {
        if (another.type.isScalar) {
            return type.isAssignableFrom(another.type);
        } else {
            return false;
        }
    }

    @Override
    public String toCode() {
        switch (type) {
            case BOOLEAN:
                return "boolean";
            case BYTE:
                return "byte";
            case CHAR:
                return "char";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case LONG:
                return "long";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case VOID:
                return "void";
            default:
                throw new RuntimeException("Unknown type:" + type.name());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ScalarType) {
            ScalarType otherScalar = (ScalarType)other;
            return type.equals(otherScalar.type);
        } else {
            return false;
        }
    }
}
