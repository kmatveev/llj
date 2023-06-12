package llj.asm.bytecode;

public abstract class ClassMemberData {

    public final ClassData classData;
    public String name;
    public boolean isStatic;

    public ClassMemberData(ClassData classData, String name, boolean isStatic) {
        this.classData = classData;
        this.name = name;
        this.isStatic = isStatic;
    }
}
