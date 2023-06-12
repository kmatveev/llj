package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.attributes.BootstrapMethodsAttribute;
import llj.packager.jclass.constants.ConstantPool;
import llj.packager.jclass.FieldInfo;
import llj.packager.jclass.FormatException;
import llj.packager.jclass.MethodInfo;
import llj.util.ref.Resolver;

import java.util.*;

public class ClassData {

    public final String name;
    public final ClassReference parent;
    public final ArrayList<ClassReference> implementedInterfaces = new ArrayList<ClassReference>();
    public final ArrayList<FieldData> fields = new ArrayList<FieldData>();
    public final ArrayList<MethodData> methods = new ArrayList<MethodData>();
    public final ArrayList<BootstrapMethodData> bootstrapMethods = new ArrayList<BootstrapMethodData>();
    public final ConstantPool constantPool;

    public List<Type> parse(String encoded) {
        return new ArrayList<Type>();
    }

    public FieldData getField(String name) {
        for(FieldData field : fields) {
            if (field.matches(name)) return field;
        }
        return null;
    }

    public MethodData getMethod(String name, List<Type> argTypes) {
        for(MethodData method : methods) {
            if (method.matches(name, argTypes)) return method;
        }
        return null;
    }

    public ClassData(ClassFileFormat format) throws FormatException {
        name = format.thisClassRef.resolve().resolveName();
        parent = format.parentClassRef.isNullRef() ? null : new ClassReference(format.parentClassRef.resolve().resolveName());
        constantPool = format.constPool;

        for (FieldInfo fieldDesc : format.fields) {
            FieldData fieldData = FieldData.read(this, fieldDesc);
            fields.add(fieldData);
        }

        // we need to handle some attributes, like BootstrapMethods, before handling methods, since method instructions like invokedynamic, will reference BootstrapMethods
        for(Attribute attr : format.getAttributes()) {
            if (attr.getType() == Attribute.AttributeType.BOOTSTRAP_METHODS) {
                for (BootstrapMethodsAttribute.BootstrapMethod bootstrapMethodDesc : ((BootstrapMethodsAttribute)attr).bootstrapMethods) {
                    bootstrapMethods.add(BootstrapMethodData.read(bootstrapMethodDesc));
                }
            }
        }

        for (MethodInfo methodDesc : format.methods) {
            MethodData methodData = MethodData.read(this, methodDesc);
            methods.add(methodData);
        }

    }

    public ClassData(String name, ClassReference parent) {
        this.name = name;
        this.parent = parent;
        this.constantPool = new ConstantPool();
    }

    public ClassData(String name, ClassReference parent, List<MethodData> methods) {
        this.name = name;
        this.parent = parent;
        this.constantPool = new ConstantPool();
        this.methods.addAll(methods);
    }

    /**
     * This method returns only "class dependencies", which is a parent class and implemented interfaces.
     * This method is not recursive, so it doesn't return dependencies of parent class and implemented interfaces.
     * For a list of classes used from methods use MethodData.getDependencies() for particular method
     *
     * @return
     */
    public Set<ClassReference> getClassDependencies() {
        Set<ClassReference> dependencies = new HashSet<ClassReference>();
        if (parent != null) {
            dependencies.add(parent);
        }
        dependencies.addAll(implementedInterfaces);
        return dependencies;
    }

    public void linkAll(Resolver<ClassData, String> classCache, boolean force) throws LinkException {

        if (this.parent != null) {
            classCache.resolveAndCache(this.parent);
        }

        for (MethodData method : methods) {
            if (!(method.isAbstract || method.isNative)) {
                if (!method.isLinked() || force) {
                    method.link(classCache);
                }
            }
        }
    }

    public boolean isSameOrSubClassOf(ClassData classData) throws UnresolvedReference {
        if (classData == this) return true;

        if (parent != null && parent.get().isSameOrSubClassOf(classData)) {
            return true;
        }

        for (ClassReference interf : implementedInterfaces) {
            if (interf.get().isSameOrSubClassOf(classData)) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        return name;
    }

    public List<String> validate() {
        ArrayList<String> result = new ArrayList<String>();

        // check that all fields have unique names

        // check that there are no methods with same name and paramset

        // check that there are no method params and fields with type 'void'

        return result;
    }

    public String[] getPackageComponents() {
        if (name.indexOf('/') < 0) return new String[0];
        String[] nameComponents = name.split("/");
        return Arrays.copyOfRange(nameComponents, 0, nameComponents.length - 1);
    }

}
