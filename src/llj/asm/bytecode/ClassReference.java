package llj.asm.bytecode;

import llj.packager.jclass.constants.ClassRefConstant;
import llj.packager.jclass.FormatException;
import llj.util.ref.StringReference;

public class ClassReference extends StringReference<ClassData> {

    public ClassReference(String id) {
        super(id);
    }

    public ClassReference(ClassData obj) {
        this(obj.name);
        this.direct = obj;
    }

    public static ClassReference make(ClassRefConstant<ClassReference> classRef) throws FormatException {
        ClassReference shortcut = classRef.getShortcut();
        if (shortcut != null) {
            return shortcut;
        } else {
            ClassReference reference = new ClassReference(classRef.resolveName());
            classRef.setShortcut(reference);
            return reference;
        }
    }

    public static ClassReference makeClassRef(String id) {
        return new ClassReference(id);
    }

    public void linkWith(ClassData classData) {
        if (this.direct != null) return;
        if (!this.id.equals(classData.name)) throw new IllegalArgumentException("Provided ClassData has a name which doesn't match referenced class name");
        this.direct = classData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassReference) {
            ClassReference anotherRef = (ClassReference)obj;
            return id.equals(anotherRef.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
