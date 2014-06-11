package llj.asm.bytecode;

import llj.util.ref.Resolver;

import java.util.EmptyStackException;

public class FieldRefInstruction extends Instruction {

    public final FieldReference fieldRef;
    public final boolean get;
    public final boolean instance;

    public FieldRefInstruction(InstructionCode code, FieldReference fieldRef) {
        super(code);
        this.fieldRef = fieldRef;
        switch (code) {
            case getfield:  instance = true;  get = true;  break;
            case putfield:  instance = true;  get = false; break;
            case getstatic: instance = false; get = true;  break;
            case putstatic: instance = false; get = false; break;
            default: throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
    }

    public FieldRefInstruction(boolean instance, boolean get, FieldReference fieldRef) {
        super(makeCode(instance, get));
        this.get = get;
        this.instance = instance;
        this.fieldRef = fieldRef;
    }

    public static InstructionCode makeCode(boolean instance, boolean get) {
        InstructionCode result;
        if (instance) {
            result = get ? InstructionCode.getfield : InstructionCode.putfield;
        } else {
            result = get ? InstructionCode.getstatic : InstructionCode.putstatic;
        }
        return result;
    }

    public Effect getEffect() {
        Effect result;
        if (instance) {
            result = get ? new GetField() : new PutField();
        } else {
            result = get ? new GetStaticField() : new PutStaticField();
        }
        return result;
    }

    @Override
    public boolean isLinked() {
        return fieldRef.isLinked();
    }

    @Override
    public void link(Resolver<ClassData, String> classCache, MethodData methodData) throws LinkException {
        fieldRef.link(classCache);
    }

    @Override
    public String toString() {
        return code.name() + " " + fieldRef.toString();
    }

    public class GetField extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, ClassesNotLoadedException {
            Type refType;
            try {
                refType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!RefType.instanceRef(fieldRef.classRef).isAssignableFrom(refType)) throw new IncompatibleStackEffectException();

            opStack.content.push(fieldRef.expectedType);
        }

    }

    public class GetStaticField extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            opStack.content.push(fieldRef.expectedType);
        }
    }

    public class PutField extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, ClassesNotLoadedException {
            Type valueType;
            try {
                valueType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!fieldRef.expectedType.isAssignableFrom(valueType)) throw new IncompatibleStackEffectException();

            Type refType;
            try {
                refType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!RefType.instanceRef(fieldRef.classRef).isAssignableFrom(refType)) throw new IncompatibleStackEffectException();
        }
    }

    public class PutStaticField extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, ClassesNotLoadedException {
            Type fieldType;
            try {
                fieldType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!fieldRef.expectedType.isAssignableFrom(fieldType)) throw new IncompatibleStackEffectException();
        }
    }

}
