package llj.asm.bytecode;

public class CompareInstruction extends Instruction {

    public final TypeType type;
    public final Effect effect;

    public CompareInstruction(InstructionCode code) {
        super(code);
        type = getType(code);
        effect = getStackEffect(code);
    }

    public static TypeType getType(InstructionCode code) {
        TypeType result;
        switch (code) {
            case lcmp : result = TypeType.LONG;   break;
            case dcmpg:
            case dcmpl: result = TypeType.DOUBLE; break;
            case fcmpg:
            case fcmpl: result = TypeType.FLOAT;  break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    private static Effect getStackEffect(InstructionCode code) {
        TypeType srcType = getType(code);
        Effect effect = Effect.make().get(ScalarType.scalar(srcType)).get(ScalarType.scalar(srcType)).put(ScalarType.scalar(TypeType.INT));
        return effect;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
