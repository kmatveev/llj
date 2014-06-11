package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

import java.lang.*;

public abstract class Value {

    static final int SIZE_SINGLE = 4;
    static final int SIZE_DOUBLE = SIZE_SINGLE * 2;

    public static int getSizeFor(TypeType type) {
        switch (type) {
            case INT:
            case FLOAT:
            case REF:
            case ARRAY_REF:
            case SHORT:
            case CHAR:
            case BYTE:
            case BOOLEAN:
                return SIZE_SINGLE;
            case DOUBLE:
            case LONG:
                return SIZE_DOUBLE;
            default:
                throw new RuntimeException();
        }
    }

//    public static Value read(TypeType type, byte[] source, int offset) {
//        if (type == TypeType.INT || type == TypeType.FLOAT ) {
//            return SIZE_SINGLE;
//        } else if (type == TypeType.REF ) {
//            return Heap.Pointer.load(source, offset);
//        } else {
//            return null;
//        }
//    }

    public abstract TypeType getType();

    public abstract int getSize();

    public abstract int getFirstWord();

    public abstract int getSecondWord();

}
