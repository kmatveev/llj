package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.InterfaceMethodRefConstant;
import llj.packager.jclass.constants.MethodRefConstant;
import llj.packager.jclass.constants.NameTypePairConstant;
import llj.util.ref.Resolver;

import java.util.List;

public class MethodReference extends ClassMemberReference<MethodData> {

    public final String methodName;
    public final List<Type> paramTypes;
    public final Type expectedReturnType;
    private MethodData method;

    public MethodReference(ClassReference classRef, String methodName, List<Type> paramTypes, Type expectedReturnType) {
        super(classRef);
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.expectedReturnType = expectedReturnType;
    }

    public static MethodReference make(MethodRefConstant<MethodReference, ClassReference> methodRef) throws FormatException {
        MethodReference shortcut = methodRef.getShortcut();
        if (shortcut != null) {
            return shortcut;
        } else {
            ClassReference classRef = ClassReference.make(methodRef.resolveClass());
            NameTypePairConstant nameTypePair = methodRef.resolveNameType();
            String methodName = nameTypePair.resolveName();
            String typeSignature = nameTypePair.resolveType();
            List<Type> paramTypes = MethodData.extractParamTypes(typeSignature);
            Type expectedType = MethodData.extractReturnType(typeSignature);
            MethodReference result = new MethodReference(classRef, methodName, paramTypes, expectedType);
            methodRef.setShortcut(result);
            return result;
        }
    }

    public static MethodReference make(InterfaceMethodRefConstant<MethodReference, ClassReference> methodRef) throws FormatException {
        MethodReference shortcut = methodRef.getShortcut();
        if (shortcut != null) {
            return shortcut;
        } else {
            ClassReference classRef = ClassReference.make(methodRef.resolveClass());
            NameTypePairConstant nameTypePair = methodRef.resolveNameType();
            String methodName = nameTypePair.resolveName();
            String typeSignature = nameTypePair.resolveType();
            List<Type> paramTypes = MethodData.extractParamTypes(typeSignature);
            Type expectedType = MethodData.extractReturnType(typeSignature);
            MethodReference result = new MethodReference(classRef, methodName, paramTypes, expectedType);
            methodRef.setShortcut(result);
            return result;
        }
    }

    public boolean isLinked() {
        return method != null;
    }

    public MethodData follow() {
        return method;
    }

    public boolean link(Resolver<ClassData, String> classCache) throws LinkException {

        for (Type paramType : paramTypes) {
            if (paramType.type == TypeType.REF) {
                ((RefType)paramType).resolveRef(classCache);
            }
        }
        if (expectedReturnType.type == TypeType.REF) {
            ((RefType)expectedReturnType).resolveRef(classCache);
        }

        if (classCache.resolveAndCache(classRef)) {
            ClassData classData = classRef.get();
            // even if ref params will not be resolved, we will be able to find method by param refs
            method = classData.getMethod(methodName, paramTypes);
            if (method == null) {
                throw new LinkException("Cannot resolve method. Class " + classData.toString() + " doesn't contain a method with name " + methodName + " and type signature ... ");
            }
            if (!method.returnType.equals(expectedReturnType)) throw new LinkException("Method return type mismatch. Expected: " + expectedReturnType + " , actual: " + method.returnType);
            return true;
        } else {
            return false;
        }
    }

    public void linkWith(MethodData methodData) {
        if (this.method != null) return;
        if (!methodName.equals(methodData.name)) throw new IllegalArgumentException("Name of provided method doesn't match referenced name");
        if (!expectedReturnType.equals(methodData.returnType)) throw new IllegalArgumentException("Method return type mismatch. Expected: " + expectedReturnType + " , actual: " + method.returnType);
        this.classRef.linkWith(methodData.classData);
        this.method = methodData;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        MethodReference otherRef = (MethodReference)obj;
        if (isLinked() && otherRef.isLinked()) {
            return follow().equals(otherRef.follow());
        } else {
            return classRef.equals(otherRef.classRef) && methodName.equals(otherRef.methodName) && paramTypes.equals(otherRef.paramTypes);
        }
    }

    public String toString() {
        if (isLinked()) {
            return follow().toString();
        } else {
            return classRef.toString() + ".#" + methodName + "()";
        }
    }

}
