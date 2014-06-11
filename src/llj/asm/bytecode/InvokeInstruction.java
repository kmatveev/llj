package llj.asm.bytecode;

import llj.util.ref.Resolver;

import java.util.EmptyStackException;
import java.util.List;

public class InvokeInstruction extends Instruction {

    public final MethodReference methodRef;
    public final boolean instance;

    public InvokeInstruction(InstructionCode code, MethodReference methodRef) {
        super(code);
        this.methodRef = methodRef;
        switch (code) {
            case invokevirtual:
            case invokeinterface:
            case invokespecial:
                instance = true; break;
            case invokestatic:
                instance = false; break;
            default:
                throw new RuntimeException("Wrong instruction code:" + code);
        }
    }

    public InvokeInstruction(boolean instance, MethodReference methodRef) {
        super(makeCode(instance));
        this.instance = instance;
        this.methodRef = methodRef;
    }

    public static InstructionCode makeCode(boolean instance) {
        return instance ? InstructionCode.invokevirtual : InstructionCode.invokestatic;
    }

    public Effect getEffect() {
        return instance ? new InvokeInstance() : new InvokeStatic();
    }

    @Override
    public boolean isLinked() {
        return methodRef.isLinked();
    }

    @Override
    public void link(Resolver<ClassData, String> classCache, MethodData methodData) throws LinkException {
        methodRef.link(classCache);
    }

    @Override
    public String toString() {
        return code.name() + " " + methodRef.toString();
    }

    public class InvokeStatic extends Effect {

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException {
            List<Type> paramTypes = methodRef.paramTypes;
            for (int i = paramTypes.size() - 1; i >= 0; i--) {
                Type t = paramTypes.get(i);
                Type valueType;
                try {
                    valueType = opStack.content.pop();
                } catch (EmptyStackException e) {
                    throw new IncompatibleStackEffectException("OpStack Underflow", e);
                }
                if (!t.isAssignableFrom(valueType)) throw new IncompatibleVariableException();
            }
            Type returnType = methodRef.expectedReturnType;
            if (returnType.type != TypeType.VOID) {
                opStack.content.push(returnType);
            }
        }
    }

    public class InvokeInstance extends Effect {

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException {
            List<Type> paramTypes = methodRef.paramTypes;
            for (int i = paramTypes.size() - 1; i >= 0; i--) {
                Type t = paramTypes.get(i);
                Type valueType;
                try {
                    valueType = opStack.content.pop();
                } catch (EmptyStackException e) {
                    throw new IncompatibleStackEffectException("OpStack Underflow", e);
                }
                if (!t.isAssignableFrom(valueType)) throw new IncompatibleVariableException();
            }
            if (!RefType.instanceRef(methodRef.classRef).isAssignableFrom(opStack.content.pop())) throw new IncompatibleVariableException();
            Type returnType = methodRef.expectedReturnType;
            if (returnType.type != TypeType.VOID) {
                opStack.content.push(returnType);
            }
        }
    }

}
