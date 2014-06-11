package llj.asm.bytecode;

import llj.packager.jclass.constants.Constant;

public enum TypeType {

    BOOLEAN('Z', true), BYTE('B', true), CHAR('C', true), SHORT('S', true), INT('I', true), LONG('J', true), FLOAT('F', true), DOUBLE('D', true), REF('L', false), ARRAY_REF('[', false), VOID('V', true), ADDRESS('X', true);

    public final char id;
    public final boolean isScalar;

    private TypeType(char id, boolean scalar) {
        this.id = id;
        isScalar = scalar;
    }

    public static TypeType parse(char id) {
        for (TypeType tt : TypeType.values()) {
            if (tt.id == id) return tt;
        }
        throw new IllegalArgumentException("Not supported:" + id);
    }

    public static int size(TypeType type) {
        switch (type) {
            case DOUBLE:
            case LONG:
                return 2;
            case INT:
            case SHORT:
            case CHAR:
            case BYTE:
            case BOOLEAN:
                return 1;
            case REF:
            case ARRAY_REF:
                return 1;
            default:
                throw new IllegalArgumentException();
        }
    }


    public boolean isAssignableFrom(TypeType type) {
        switch (this) {
            case INT:
                return type == INT || type == SHORT || type == CHAR || type == BOOLEAN;
            case REF:
                return type == REF || type == ARRAY_REF;
            default:
                return type == this;
        }
    }

    public static TypeType valueOf(Constant.ConstType type) {
        switch (type) {
            case INTEGER:
                return INT;
            case FLOAT:
                return FLOAT;
            case DOUBLE:
                return DOUBLE;
            case LONG:
                return LONG;
            case STRING_REF:
                return REF;
            case CLASS_REF:
                return REF;
            default:
                throw new RuntimeException();
        }
    }
}
