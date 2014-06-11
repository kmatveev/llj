package llj.asm.bytecode;

public class RefType extends Type {

    public final ClassReference classRef;

    public RefType(ClassReference classRef) {
        super(classRef != null && classRef.isResolved() && ClassIntrinsics.isArrayClassData(classRef.get()) ? TypeType.ARRAY_REF : TypeType.REF);
        if (!(this instanceof ArrayRefType) && classRef != null && classRef.isResolved() && ClassIntrinsics.isArrayClassData(classRef.get())) throw new IllegalArgumentException("Attempt to create an array type with wrong class");
        this.classRef = classRef;
    }

    public static RefType instanceRef(ClassReference classRef) {
        if (classRef == null) throw new IllegalArgumentException();
        return new RefType(classRef);
    }

    public static RefType anyRef() {
        return new RefType(null);
    }

    @Override
    public boolean isAssignableFrom(Type another) throws ClassesNotLoadedException {
        if (type == TypeType.REF) {
            if (another.type.isScalar) {
                return false;
            } else if (another.type == TypeType.REF) {
                RefType anotherRef = (RefType)another;
                if (classRef == null) {
                    // this means "any object reference"
                    return true;
                } else {
                    try {
                        return classRef.equals(anotherRef.classRef) || anotherRef.classRef.get().isSameOrSubClassOf(classRef.get());
                    } catch (UnresolvedReference e) {
                        throw new ClassesNotLoadedException(classRef, anotherRef.classRef);
                    }
                }
            } else if (another.type == TypeType.ARRAY_REF) {
                return classRef.equals(ClassIntrinsics.OBJECT_CLASS_REF);
            } else {
                throw new RuntimeException();
            }
        } else {
            // must not be called from sub-classes
            throw new RuntimeException();
        }

    }

    @Override
    public String toCode() {
        switch (type) {
            case REF:
                return classRef.isResolved() ? classRef.get().name : String.valueOf(classRef.id);
            default:
                throw new RuntimeException("Unknown type:" + type.name());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RefType) {
            RefType anotherRef = (RefType)other;
            return classRef == null ? anotherRef.classRef == null : classRef.equals(anotherRef.classRef);
        } else {
            return false;
        }
    }
}
