package llj.asm.bytecode;

public class UnOpInstruction extends Instruction {

    public final TypeType type;
    public final Effect effect;

    public UnOpInstruction(InstructionCode code) {
        super(code);
        this.type = getType(code);
        effect = makeUnOp(type);
    }

    public static TypeType getType(InstructionCode code) {
        TypeType result;
        switch (code) {
            case dneg: result = TypeType.DOUBLE; break;
            case fneg: result = TypeType.FLOAT; break;
            case ineg: result = TypeType.INT; break;
            case lneg: result = TypeType.LONG; break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    public static Effect.Static makeUnOp(TypeType type) {
        return Effect.make().get(ScalarType.scalar(type)).put(ScalarType.scalar(type));
        // return new Effect.Static(new Effect.Static.StackOp[] { new Effect.Static.StackOp(Type.scalar(type), Effect.Direction.GET), new Effect.Static.StackOp(Type.scalar(type), Effect.Direction.PUT) });
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
