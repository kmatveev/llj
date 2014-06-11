package llj.asm.bytecode.exec;

import llj.asm.bytecode.ArrayRefType;
import llj.asm.bytecode.ClassData;
import llj.asm.bytecode.ClassIntrinsics;
import llj.asm.bytecode.ClassReference;
import llj.asm.bytecode.MethodReference;
import llj.asm.bytecode.RefType;
import llj.asm.bytecode.Type;
import llj.packager.jclass.ClassFileFormat;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VM {

    public final ClassHeap classHeap = new ClassHeap();
    public final Heap heap = new Heap();

    public final List<File> classPath = new ArrayList<File>();

    public void execute(String mainClassName, String[] classpathStr) {

        populateClassPath(classpathStr);

        try {
            loadEssentialClasses();
        } catch (ClassLoadingTrouble e) {
            System.out.println("Cannot load platform essential class; reason:" + e.getMessage());
            return;
        }


        ClassRuntimeData runtimeMainClass;
        try {
            runtimeMainClass = loadClass(new ClassReference(mainClassName));
        } catch (ClassLoadingTrouble e) {
            System.out.println("Cannot load main class; reason:" + e.getMessage());
            return;
        }

        List<Type> mainMethodArgs = new ArrayList<Type>();
        mainMethodArgs.add(ArrayRefType.arrayOf(RefType.instanceRef(new ClassReference(ClassIntrinsics.STRING_CLASS_NAME))));

        try {
            MethodRuntimeData runtimeMainMethod = runtimeMainClass.getRuntimeData(new MethodReference(new ClassReference(runtimeMainClass.classData.name), "main", mainMethodArgs, null));
            runtimeMainMethod.link(this);

            ThreadState threadState = new ThreadState();
            // TODO allocate string array and populate it with command-line args
            threadState.currentFrame().pushOp(heap.nullPointer);
            threadState.enterStatic(runtimeMainMethod);
            Interpreter.interpret(threadState, this);
        } catch (RuntimeTrouble e) {
            System.out.println("RuntimeTrouble:" + e.getMessage());
        }

    }

    public ClassRuntimeData loadClass(ClassReference classRef) throws ClassLoadingTrouble {
        return loadClass(classRef, new HashSet<ClassReference>());
    }

    private ClassRuntimeData loadClass(ClassReference selfRef, Set<ClassReference> pending) throws ClassLoadingTrouble {

        ClassRuntimeData existing = classHeap.get(selfRef);
        if (existing != null) return existing;

        ClassData classData = ClassIntrinsics.get(selfRef);

        if (classData == null) {
            File mainClassFile = find(classPath, classNameToPath(selfRef.id, '.'));
            if (mainClassFile == null) {
                throw new ClassLoadingTrouble("Class file not found:" + selfRef.id);
            }

            try {
                FileInputStream inputStream = new FileInputStream(mainClassFile);
                ClassFileFormat rawMainClass = ClassFileFormat.readFrom(inputStream.getChannel());
                classData = new ClassData(rawMainClass);
            } catch (Exception e) {
                throw new ClassLoadingTrouble(e);
            }
        }

        ClassRuntimeData runtimeMainClass = prepare(classData);
        return runtimeMainClass;

    }

    public ClassRuntimeData prepare(ClassData mainClass) throws ClassLoadingTrouble {
        Set<ClassReference> dependencies = mainClass.getClassDependencies();
        // TODO track class circularity
        loadClasses(dependencies);
        return classHeap.getOrCreate(mainClass);
    }

    public void populateClassPath(String[] classpathStr) {
        for (String classPathEntryStr : classpathStr) {
            File classPathEntry = new File(classPathEntryStr);
            if (classPathEntry.exists()) {
                classPath.add(classPathEntry);
            }
        }
    }

    public void loadClasses(Collection<ClassReference> classes) throws ClassLoadingTrouble{
        for (ClassReference classRef : classes) {
            loadClass(classRef);
        }
    }

    public static File find(List<File> roots, String relativePath) {
        for (File root : roots) {
            File possibleLocation = new File(root, relativePath);
            if (possibleLocation.exists()) return possibleLocation;
        }
        return null;
    }

    public static String classNameToPath(String className) {
        return classNameToPath(className, '/');
    }

    public static String classNameToPath(String className, char sourceSeparator) {
        return className.replace(sourceSeparator, File.separatorChar) + ".class";
    }

    public void loadEssentialClasses() throws ClassLoadingTrouble {
        loadClass(ClassIntrinsics.OBJECT_CLASS_REF);
        loadClass(ClassIntrinsics.CLASS_CLASS_REF);
        loadClass(ClassIntrinsics.STRING_CLASS_REF);
        // TODO Throwable, Exception and some runtime exceptions
    }

}
