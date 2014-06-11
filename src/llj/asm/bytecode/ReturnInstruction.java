package llj.asm.bytecode;

import java.util.EmptyStackException;

public class ReturnInstruction extends Instruction {

    public final TypeType type;
    private final Effect effect;

    public ReturnInstruction(InstructionCode code) {
        super(code);
        this.type = getType(code);
        effect = new ReturnEffect();
    }

    public ReturnInstruction(TypeType type) {
        super(makeCode(type));
        this.type = getType(code);
        effect = new ReturnEffect();
    }

    @Override
    public Effect getEffect() {
        return effect;
    }

    class ReturnEffect extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException {
            if (type != TypeType.VOID) {
                Type returnedType;
                try {
                    returnedType = opStack.content.pop();
                } catch (EmptyStackException e) {
                    throw new IncompatibleStackEffectException("OpStack Underflow", e);
                }
                if (returnedType.type != type) throw new IncompatibleStackEffectException();
            }
        }
    }

    public static TypeType getType(InstructionCode code) {
        TypeType result;
        switch (code) {
            case _return: result = TypeType.VOID;   break;
            case areturn: result = TypeType.REF;    break;
            case ireturn: result = TypeType.INT;    break;
            case lreturn: result = TypeType.LONG;   break;
            case freturn: result = TypeType.FLOAT;  break;
            case dreturn: result = TypeType.DOUBLE; break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    public static InstructionCode makeCode(TypeType type) {
        InstructionCode result;
        if (type == TypeType.VOID) {
            result = InstructionCode._return;
        } else {
            switch (type) {
                case REF:    result = InstructionCode.areturn; break;
                case INT:    result = InstructionCode.ireturn; break;
                case LONG:   result = InstructionCode.lreturn; break;
                case FLOAT:  result = InstructionCode.freturn; break;
                case DOUBLE: result = InstructionCode.dreturn; break;
                default: throw new IllegalArgumentException();
            }
        }
        return result;
    }


}
