package llj.asm.bytecode;

import llj.packager.jclass.AccessFlags;
import llj.packager.jclass.MethodInfo;
import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.attributes.Code;
import llj.packager.jclass.FormatException;
import llj.packager.jclass.attributes.StackMapTable;
import llj.util.ref.Resolver;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodData extends ClassMemberData {

    public String name;
    public boolean isStatic, isAbstract, isNative;
    public List<Type> params;
    public Type returnType;
    public List<Instruction> code;
    public int stackFrameSize, opStackSize;
    public MethodStaticInfo loadedStaticInfo;

    public MethodData(ClassData classData, String name, boolean isStatic, boolean isAbstract, boolean isNative, List<Type> params, Type returnType, List<Instruction> code, int stackFrameSize, int opStackSize) {
        super(classData);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.code = code;
        this.isStatic = isStatic;
        this.isAbstract = isAbstract;
        this.isNative = isNative;
        this.stackFrameSize = stackFrameSize;
        this.opStackSize = opStackSize;
    }

    public static CharBuffer parseParams(String descriptor) throws FormatException {
        if (descriptor.charAt(0) != '(') throw new FormatException("Method params section should start with '('");
        int i = descriptor.indexOf(')', 1);
        if (i < 0 ) throw new FormatException("Method params section should end with ')'");
        if (descriptor.indexOf('(', 1) >= 0) throw new FormatException("Method params section should contain only one '('");
        if (descriptor.indexOf('(', i + 1) >= 0) throw new FormatException("Method params section should contain only one ')'");
        return CharBuffer.wrap(descriptor.substring(1, i));
    }

    public static CharBuffer parseReturnValue(String descriptor) throws FormatException {
        int i = descriptor.indexOf(')', 1);
        if (i < 0 ) throw new FormatException("Method params section should end with ')'");
        return CharBuffer.wrap(descriptor.substring(i + 1));
    }

    public static Type extractReturnType(String descriptor) throws FormatException {
        Type returnType;
        try {
            CharBuffer returnTypeStr = parseReturnValue(descriptor);
            returnType = Type.fromFormat(returnTypeStr);
        } catch (IOException e) {
            throw new FormatException(e);
        }
        return returnType;
    }

    public static List<Type> extractParamTypes(String descriptor) throws FormatException {
        CharBuffer paramTypes = parseParams(descriptor);

        List<Type> params = new ArrayList<Type>();
        while (paramTypes.hasRemaining()) {
            try {
                Type type = Type.fromFormat(paramTypes);
                params.add(type);
            } catch (IOException e) {
                throw new FormatException(e);
            }
        }
        return params;
    }

    public static MethodData read(ClassData classData, MethodInfo methodDesc) throws FormatException {
        String methodName = methodDesc.resolveName();
        String descriptor = methodDesc.resolveDescriptor();

        List<Type> params = extractParamTypes(descriptor);
        Type returnType = extractReturnType(descriptor);

        boolean isStatic = methodDesc.accessFlags.contains(AccessFlags.STATIC);
        boolean isAbstract = methodDesc.accessFlags.contains(AccessFlags.ABSTRACT);
        boolean isNative = methodDesc.accessFlags.contains(AccessFlags.NATIVE);

        List<Instruction> code = null;
        StackMapTable stackMapTable = null;
        int stackFrameSize = 0, opStackSize = 0;
        for (Attribute attr : methodDesc.attributes) {
            if (attr.getType() == Attribute.AttributeType.CODE) {
                if (isAbstract) {
                    throw new FormatException("Abstract method contains code attribute");
                }
                Code codeDesc = (Code)attr;
                code = Instruction.readCode(codeDesc.code, classData.constantPool);
                stackFrameSize = codeDesc.maxLocals + 1;
                opStackSize = codeDesc.maxStack + 1;
                for (Attribute codeAttr : codeDesc.attributes) {
                    if (codeAttr.getType() == Attribute.AttributeType.STACK_MAP_TABLE) {
                        stackMapTable = (StackMapTable) codeAttr;
                    }
                }
            }
        }
        if (code == null && !(isNative || isAbstract)) throw new FormatException("Method doesn't contain code");

        final MethodData methodData = new MethodData(classData, methodName, isStatic, isAbstract, isNative, params, returnType, code, stackFrameSize, opStackSize);

        if (stackMapTable != null) {
            methodData.loadedStaticInfo = MethodStaticInfo.load(methodData, stackMapTable);
        }

        return methodData;
    }

    public boolean matches(String name, List<Type> argTypes) {
        return this.name.equals(name) && this.params.equals(argTypes);
    }

    public boolean isAbstract() {
        return code == null;
    }

    public Instruction atByteOffset(int byteOffset) {
        for (Instruction instruction : code) {
            if (instruction.byteOffset == byteOffset) {
                return instruction;
            }
        }
        throw new IllegalArgumentException("Provided byte offset doesn't point to instruction:" + byteOffset + " in method " + toString());
    }

    public Set<ClassReference> getDependencies() {
        HashSet<ClassReference> result = new HashSet<ClassReference>();
        for (Instruction instr : code) {
            if (instr instanceof FieldRefInstruction) {
                FieldRefInstruction fieldRefInstruction = (FieldRefInstruction)instr;
                result.add(fieldRefInstruction.fieldRef.classRef);
            } else if (instr instanceof InvokeInstruction) {
                InvokeInstruction invokeInstruction = (InvokeInstruction)instr;
                result.add(invokeInstruction.methodRef.classRef);
            } else if (instr instanceof NewInstanceInstruction) {
                NewInstanceInstruction newInstanceInstruction = (NewInstanceInstruction)instr;
                result.add(newInstanceInstruction.classRef);
            } else if (instr instanceof NewArrayInstruction) {
                NewArrayInstruction newArrayInstruction = (NewArrayInstruction)instr;
                if (newArrayInstruction.arrayElemType.type == TypeType.REF) {
                    RefType refElemType = (RefType)newArrayInstruction.arrayElemType;
                    result.add(refElemType.classRef);
                }
            } else if (instr instanceof InstanceofInstruction) {
                InstanceofInstruction instanceofInstruction = (InstanceofInstruction)instr;
                result.add(instanceofInstruction.classRef);
            } else if (instr instanceof LoadConstInstruction) {
                LoadConstInstruction instanceofInstruction = (LoadConstInstruction)instr;
                if (instanceofInstruction.constantData.type.type == TypeType.REF) {
                    RefType refConstType = (RefType) instanceofInstruction.constantData.type;
                    if (refConstType.classRef.equals(ClassIntrinsics.STRING_CLASS_REF)) {
                        result.add(new ClassReference(ClassIntrinsics.charArrayClassData));
                        result.add(ClassIntrinsics.STRING_CLASS_REF);
                    } else if (refConstType.classRef.equals(ClassIntrinsics.CLASS_CLASS_REF)) {
                        result.add(ClassIntrinsics.CLASS_CLASS_REF);
                    }
                }
            }
        }
        return result;
    }

    public void link(Resolver<ClassData, String> classCache) throws LinkException {
        for (Instruction instr : code) {
            instr.link(classCache, this);
        }
    }

    public boolean isLinked() {
        for (Instruction instr : code) {
            if (instr.isLinked()) return false;
        }
        return true;
    }


    public List<String> validate() {
        // TODO check if static method uses field/method access
        return null;
    }

    @Override
    public String toString() {
        return classData.toString() + "." + name + "()";
    }

    public String toSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.toCode());
        sb.append(" ");
        sb.append(name);
        sb.append("(");
        boolean first = true;
        for (Type param : params) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(param.toCode());
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isAllowedForParameter(Type type) {
        return FieldData.isAllowed(type);
    }

    public boolean isAllowedForResult(Type type) {
        return true;
    }

    public static int localVarSlotsFor(List<Type> params) {
        int i = 0;
        for (Type paramType : params) {
            i += TypeType.size(paramType.type);  // some vars (of long and double types) occupy two slots in local var tables
        }
        return i;
    }

}
