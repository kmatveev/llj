package llj.asm.bytecode;

public class ClassesNotLoadedException extends Exception {

    public final ClassReference[] classesToLoad;

    public ClassesNotLoadedException(ClassReference[] classesToLoad) {
        this.classesToLoad = classesToLoad;
    }

    public ClassesNotLoadedException(ClassReference classToLoad) {
        this.classesToLoad = new ClassReference[] { classToLoad };
    }

    public ClassesNotLoadedException(ClassReference classToLoadA, ClassReference classToLoadB) {
        this.classesToLoad = new ClassReference[] { classToLoadA, classToLoadB };
    }

}
