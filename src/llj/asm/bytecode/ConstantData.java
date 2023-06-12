package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.*;

public class ConstantData {

    public final Type type;
    public final Object value;


    private ConstantData(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static ConstantData get(Constant source) throws FormatException {

        switch (source.getType()) {
            case INTEGER:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = new Integer(((IntegerConstant<ConstantData>)source).value);
                    Type type = ScalarType.scalar(TypeType.valueOf(source.getType()));
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case LONG:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = new Long(((LongConstant<ConstantData>)source).value);
                    Type type = ScalarType.scalar(TypeType.valueOf(source.getType()));
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case FLOAT:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = new Float(((FloatConstant<ConstantData>)source).value);
                    Type type = ScalarType.scalar(TypeType.valueOf(source.getType()));
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case DOUBLE:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = new Double(((DoubleConstant<ConstantData>)source).value);
                    Type type = ScalarType.scalar(TypeType.valueOf(source.getType()));
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case CLASS_REF:
            {
                ClassReference value;
                ClassReference shortcut = (ClassReference)source.getShortcut();
                if (shortcut != null) {
                    value = shortcut;
                } else {
                    value = new ClassReference(((ClassRefConstant)source).resolveName());
                    source.setShortcut(value);
                }
                Type type = RefType.instanceRef(ClassIntrinsics.CLASS_CLASS_REF);
                ConstantData result = new ConstantData(type, value);
                return result;
            }
            case STRING_REF:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = ((StringRefConstant)source).resolveValue();
                    Type type = RefType.instanceRef(ClassIntrinsics.STRING_CLASS_REF);
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case METHOD_TYPE:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = MethodTypeData.read(((MethodTypeConstant)source).typeRef);
                    Type type = RefType.instanceRef(ClassIntrinsics.METHOD_TYPE_CLASS_REF);
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            case METHOD_HANDLE:
            {
                ConstantData shortcut = (ConstantData)source.getShortcut();
                if (shortcut != null) {
                    return shortcut;
                } else {
                    Object value = MethodHandle.read((MethodHandleConstant)source);
                    Type type = RefType.instanceRef(ClassIntrinsics.METHOD_HANDLE_CLASS_REF);
                    ConstantData result = new ConstantData(type, value);
                    source.setShortcut(result);
                    return result;
                }
            }
            default:
                throw new RuntimeException();


        }

    }

    @Override
    public String toString() {
        return "<" + type.toString() + ">" + String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        ConstantData otherObj = (ConstantData)obj;
        return otherObj.type.equals(this.type) && otherObj.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
