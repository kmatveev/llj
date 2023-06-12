package llj.asm.bytecode;

// import llj.asm.bytecode.exec.FieldRuntimeData;
import llj.packager.jclass.AccessFlags;
import llj.packager.jclass.FieldInfo;
import llj.packager.jclass.FormatException;

public class FieldData extends ClassMemberData {

    public final Type type;
    // public FieldRuntimeData runtimeData;

    public FieldData(ClassData classData, String name, Type type, boolean isStatic) {
        super(classData, name, false);
        this.type = type;
    }

    public static boolean isAllowed(Type type) {
        return type.type != TypeType.VOID;
    }

    public static FieldData read(ClassData classData, FieldInfo fieldDesc) throws FormatException {
        String fieldName = fieldDesc.resolveName();
        String typeFormat = fieldDesc.resolveDescriptor();
        boolean isStatic = fieldDesc.accessFlags.contains(AccessFlags.STATIC);
        return new FieldData(classData, fieldName, Type.fromFormat(typeFormat), isStatic);
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    public String toString() {
        return classData.toString() + "." + name;
    }

//    public static Reference<FieldData> readFrom(InstructionCode instr) {
//
//    }
}
