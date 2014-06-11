package llj.asm.bytecode;

public class ClassIntrinsics {

    public static final String OBJECT_CLASS_NAME = "java/lang/Object";
    public static final ClassReference OBJECT_CLASS_REF = ClassReference.makeClassRef(OBJECT_CLASS_NAME);
    public static final String STRING_CLASS_NAME = "java/lang/String";
    public static final ClassReference STRING_CLASS_REF = ClassReference.makeClassRef(STRING_CLASS_NAME);
    public static final String CLASS_CLASS_NAME = "java/lang/Class";
    public static final ClassReference CLASS_CLASS_REF = ClassReference.makeClassRef(CLASS_CLASS_NAME);
    public static final String THROWABLE_CLASS_NAME = "java/lang/Throwable";
    public static final ClassReference TROWABLE_CLASS_REF = ClassReference.makeClassRef(THROWABLE_CLASS_NAME);

    public final static ClassData boolClassData    = new ClassData("boolean", OBJECT_CLASS_REF);
    public final static ClassData byteClassData    = new ClassData("byte", OBJECT_CLASS_REF);
    public final static ClassData charClassData    = new ClassData("char", OBJECT_CLASS_REF);
    public final static ClassData shortClassData   = new ClassData("short", OBJECT_CLASS_REF);
    public final static ClassData intClassData     = new ClassData("int", OBJECT_CLASS_REF);
    public final static ClassData longClassData    = new ClassData("long", OBJECT_CLASS_REF);
    public final static ClassData floatClassData   = new ClassData("float", OBJECT_CLASS_REF);
    public final static ClassData doubleClassData  = new ClassData("double", OBJECT_CLASS_REF);
    public final static ClassData voidClassData    = new ClassData("void", OBJECT_CLASS_REF);

    public final static ClassData boolArrayClassData    = new ClassData("[boolean", OBJECT_CLASS_REF);
    public final static ClassData byteArrayClassData    = new ClassData("[byte", OBJECT_CLASS_REF);
    public final static ClassData charArrayClassData    = new ClassData("[char", OBJECT_CLASS_REF);
    public final static ClassData shortArrayClassData   = new ClassData("[short", OBJECT_CLASS_REF);
    public final static ClassData intArrayClassData     = new ClassData("[int", OBJECT_CLASS_REF);
    public final static ClassData longArrayClassData    = new ClassData("[long", OBJECT_CLASS_REF);
    public final static ClassData floatArrayClassData   = new ClassData("[float", OBJECT_CLASS_REF);
    public final static ClassData doubleArrayClassData  = new ClassData("[double", OBJECT_CLASS_REF);
    public final static ClassData refArrayClassData     = new ClassData("[", OBJECT_CLASS_REF);

    public static final ClassReference boolArrayClassRef   = new ClassReference(boolArrayClassData);
    public static final ClassReference byteArrayClassRef   = new ClassReference(byteArrayClassData);
    public static final ClassReference charArrayClassRef   = new ClassReference(charArrayClassData);
    public static final ClassReference shortArrayClassRef  = new ClassReference(shortArrayClassData);
    public static final ClassReference intArrayClassRef    = new ClassReference(intArrayClassData);
    public static final ClassReference longArrayClassRef   = new ClassReference(longArrayClassData);
    public static final ClassReference floatArrayClassRef  = new ClassReference(floatArrayClassData);
    public static final ClassReference doubleArrayClassRef = new ClassReference(doubleArrayClassData);
    public static final ClassReference refArrayClassRef    = new ClassReference(refArrayClassData);

    public static ClassData getPrimitiveClassFor(TypeType type) {
        switch (type) {
            case BOOLEAN: return boolClassData;
            case BYTE: return byteClassData;
            case CHAR: return charClassData;
            case SHORT: return shortClassData;
            case INT: return intClassData;
            case LONG: return longClassData;
            case FLOAT: return floatClassData;
            case DOUBLE: return doubleClassData;
            case VOID: return voidClassData;

            default: throw new IllegalArgumentException("Type is not supported:" + type);
        }
    }

    public static TypeType arrayElementTypeFor(ClassData classData) {
        if (classData == refArrayClassData) {
            return TypeType.REF;
        } else if (classData == boolArrayClassData) {
            return TypeType.BOOLEAN;
        } else if (classData == byteArrayClassData) {
            return TypeType.BYTE;
        } else if (classData == charArrayClassData) {
            return TypeType.CHAR;
        } else if (classData == shortArrayClassData) {
            return TypeType.SHORT;
        } else if (classData == intArrayClassData) {
            return TypeType.INT;
        } else if (classData == longArrayClassData) {
            return TypeType.LONG;
        } else if (classData == floatClassData) {
            return TypeType.FLOAT;
        } else if (classData == doubleClassData) {
            return TypeType.DOUBLE;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static boolean isArrayClassData(ClassData classData) {
        if (classData == boolArrayClassData) {
            return true;
        } else if (classData == byteArrayClassData) {
            return true;
        } else if (classData == charArrayClassData) {
            return true;
        } else if (classData == shortArrayClassData) {
            return true;
        } else if (classData == intArrayClassData) {
            return true;
        } else if (classData == longArrayClassData) {
            return true;
        } else if (classData == floatArrayClassData) {
            return true;
        } else if (classData == doubleArrayClassData) {
            return true;
        } else if (classData == refArrayClassData) {
            return true;
        } else {
            return false;
        }
    }

    public static ClassData get(ClassReference ref) {

        if (ref.equals(boolArrayClassRef) || ref.id.equals(boolArrayClassData.name)) {
            return boolArrayClassData;
        } else if (ref.equals(byteArrayClassRef) || ref.id.equals(byteArrayClassData.name)) {
            return byteArrayClassData;
        } else if (ref.equals(charArrayClassRef) || ref.id.equals(charArrayClassData.name)) {
            return charArrayClassData;
        } else if (ref.equals(shortArrayClassRef) || ref.id.equals(shortArrayClassData.name)) {
            return shortArrayClassData;
        } else if (ref.equals(intArrayClassRef) || ref.id.equals(intArrayClassData.name)) {
            return intArrayClassData;
        } else if (ref.equals(longArrayClassRef) || ref.id.equals(longArrayClassData.name)) {
            return longArrayClassData;
        } else if (ref.equals(floatArrayClassRef) || ref.id.equals(floatArrayClassData.name)) {
            return floatArrayClassData;
        } else if (ref.equals(doubleArrayClassRef) || ref.id.equals(doubleArrayClassData.name)) {
            return doubleArrayClassData;
        } else if (ref.equals(refArrayClassRef) || ref.id.equals(refArrayClassData.name)) {
            return refArrayClassData;
        } else {
            return null;
        }

    }

    public static ClassData getArrayClassData(TypeType type) {
        switch (type) {
            case BOOLEAN: return boolArrayClassData;
            case BYTE: return byteArrayClassData;
            case CHAR: return charArrayClassData;
            case SHORT: return shortArrayClassData;
            case INT: return intArrayClassData;
            case LONG: return longArrayClassData;
            case FLOAT: return floatArrayClassData;
            case DOUBLE: return doubleArrayClassData;
            case REF: return refArrayClassData;
            case ARRAY_REF: return refArrayClassData;
            default: throw new IllegalArgumentException("Type is not supported:" + type);
        }
    }

}
