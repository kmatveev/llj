package llj.asm.bytecode;

import llj.util.ref.Resolver;

public abstract class ClassMemberReference<T extends ClassMemberData> {

    public final ClassReference classRef;

    public ClassMemberReference(ClassReference classRef) {
        this.classRef = classRef;
    }

    public abstract T follow();

    public abstract boolean isLinked();

    public abstract boolean link(Resolver<ClassData, String> classCache) throws LinkException;

}
