package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.FieldRefConstant;
import llj.packager.jclass.constants.NameTypePairConstant;
import llj.util.ref.Resolver;

public class FieldReference extends ClassMemberReference<FieldData> {

    public final String fieldName;
    public final Type expectedType;

    private FieldData field;

    public FieldReference(ClassReference classRef, String fieldName, Type expectedType) {
        super(classRef);
        this.fieldName = fieldName;
        this.expectedType = expectedType;
    }

    public static FieldReference make(FieldRefConstant<FieldReference, ClassReference> fieldRef) throws FormatException {
        FieldReference shortcut = fieldRef.getShortcut();
        if (shortcut != null) {
            return shortcut;
        } else {
            ClassReference classRef = ClassReference.make(fieldRef.resolveClass());
            NameTypePairConstant nameTypePair = fieldRef.resolveNameType();
            String fieldName = nameTypePair.resolveName();
            Type expectedType = Type.fromFormat(nameTypePair.resolveType());
            FieldReference result = new FieldReference(classRef, fieldName, expectedType);
            fieldRef.setShortcut(result);
            return result;
        }
    }

    public boolean isLinked() {
        return field != null;
    }

    public FieldData follow() {
        return field;
    }

    public boolean link(Resolver<ClassData, String> classCache) throws LinkException {
        ClassReference classRef = this.classRef;
        while (classRef != null) {
            if (classCache.resolveAndCache(classRef)) {
                ClassData classData = classRef.get();
                FieldData field = classData.getField(fieldName);
                if (field != null) {
                    if (!field.type.equals(expectedType)) throw new LinkException("Field type mismatch. Expected: " + expectedType + " , actual: " + field.type);
                    this.field = field;
                    return true;
                } else {
                    classRef = classData.parent;
                    // and continue loop
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void linkWith(FieldData fieldData) {
        if (this.field != null) return;
        if (!fieldName.equals(fieldData.name)) throw new IllegalArgumentException("Name of provided field doesn't match referenced name");
        if (!expectedType.type.equals(fieldData.type)) throw new IllegalArgumentException("Field type mismatch. Expected: " + expectedType + " , actual: " + field.type);
        this.classRef.linkWith(fieldData.classData);
        this.field = fieldData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        FieldReference otherRef = (FieldReference)obj;
        if (isLinked() && otherRef.isLinked()) {
            return follow().equals(otherRef.follow());
        } else {
            return classRef.equals(otherRef.classRef) && fieldName.equals(otherRef.fieldName);
        }
    }

    public String toString() {
        if (isLinked()) {
            return follow().toString();
        } else {
            return classRef.toString() + ".#" + fieldName + "()";
        }
    }


}
