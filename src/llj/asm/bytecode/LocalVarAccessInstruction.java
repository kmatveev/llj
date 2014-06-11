package llj.asm.bytecode;

import java.util.EmptyStackException;

public class LocalVarAccessInstruction extends Instruction {

    public final int localVarA;
    public final TypeType type;
    public final boolean load;
    public final Effect effect;

    public LocalVarAccessInstruction(InstructionCode code, int localVarA) {
        super(code);

        switch (code) {
            case aload_0:
            case aload_1:
            case aload_2:
            case aload_3:
            case aload:  type = TypeType.REF;    load = true;  break;
            case astore_0:
            case astore_1:
            case astore_2:
            case astore_3:
            case astore: type = TypeType.REF;    load = false; break;
            case dload_0:
            case dload_1:
            case dload_2:
            case dload_3:
            case dload:  type = TypeType.DOUBLE; load = true;  break;
            case dstore_0:
            case dstore_1:
            case dstore_2:
            case dstore_3:
            case dstore: type = TypeType.DOUBLE; load = false; break;
            case fload_0:
            case fload_1:
            case fload_2:
            case fload_3:
            case fload:  type = TypeType.FLOAT;  load = true;  break;
            case fstore_0:
            case fstore_1:
            case fstore_2:
            case fstore_3:
            case fstore: type = TypeType.FLOAT;  load = false; break;
            case iload_0:
            case iload_1:
            case iload_2:
            case iload_3:
            case iload:  type = TypeType.INT;    load = true;  break;
            case istore_0:
            case istore_1:
            case istore_2:
            case istore_3:
            case istore: type = TypeType.INT;    load = false; break;
            case lload_0:
            case lload_1:
            case lload_2:
            case lload_3:
            case lload:  type = TypeType.LONG;   load = true;  break;
            case lstore_0:
            case lstore_1:
            case lstore_2:
            case lstore_3:
            case lstore: type = TypeType.LONG;   load = false; break;
            default: throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        this.localVarA = localVarA;
        this.effect = load ? new LoadVarEffect() : new StoreVarEffect();
    }

    public LocalVarAccessInstruction(boolean load, TypeType type, int localVarA) {
        super(makeCode(load, type, localVarA));
        this.load = load;
        this.type = type;
        this.localVarA = localVarA;
        this.effect = load ? new LoadVarEffect() : new StoreVarEffect();
    }

    // TODO optimize code based on localVarA
    public static InstructionCode makeCode(boolean load, TypeType type, int localVarA) {
        switch (type) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case CHAR:
            case INT:
                return load ? InstructionCode.iload : InstructionCode.istore;
            case FLOAT:
                return load ? InstructionCode.fload : InstructionCode.fstore;
            case DOUBLE:
                return load ? InstructionCode.dload : InstructionCode.dstore;
            case LONG:
                return load ? InstructionCode.lload : InstructionCode.lstore;
            case REF:
                return load ? InstructionCode.aload : InstructionCode.astore;
            default:
                throw new IllegalArgumentException("Wrong type code:" + type);
        }
    }

    private class LoadVarEffect extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException {
            Type varType = localVarTypes.get(localVarA);
            // TODO need to distinguish these two cases: access to uninitialized variable means control-flow validation error
            if (varType == null || !type.isAssignableFrom(varType.type)) throw new IncompatibleVariableException();
            opStack.content.push(varType);
        }
    }

    private class StoreVarEffect extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException {
            Type valueType;
            try {
                valueType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!type.isAssignableFrom(valueType.type)) throw new IncompatibleVariableException();
            if (!localVarTypes.assign(localVarA, valueType)) throw new IncompatibleVariableException();
        }
    }

    @Override
    public String toString() {
        return code.name() + " " + localVarA;
    }

    public Effect getEffect() {
        return effect;
    }
}
