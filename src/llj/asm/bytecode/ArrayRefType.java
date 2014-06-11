package llj.asm.bytecode;

public class ArrayRefType extends RefType {

    // may be null, which means "any array" (useful, for example, as input type of 'arraylength' instruction)
    public final Type elemType;

    public ArrayRefType(Type elemType) {
        super(elemType == null ? null : new ClassReference(ClassIntrinsics.getArrayClassData(elemType.type)));
        this.elemType = elemType;
    }

    public static ArrayRefType arrayOf(Type elemType) {
        if (elemType == null) throw new IllegalArgumentException();
        return new ArrayRefType(elemType);
    }

    public static ArrayRefType anyArray() {
        return new ArrayRefType(null);
    }

    @Override
    public boolean isAssignableFrom(Type another) throws ClassesNotLoadedException {
        if (another.type == TypeType.ARRAY_REF) {
            ArrayRefType anotherArrayRef = (ArrayRefType)another;
            if (elemType == null) {
                // this means "any array"
                return true;
            } else {
                return elemType.isAssignableFrom(anotherArrayRef.elemType);
            }
        } else {
            return false;
        }

    }

    @Override
    public String toCode() {
        return elemType.toCode() + "[]";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ArrayRefType) {
            ArrayRefType anotherArrayRef = (ArrayRefType)other;
            return elemType == null ? anotherArrayRef.elemType == null : elemType.equals(anotherArrayRef.elemType);
        } else {
            return false;
        }
    }
}
