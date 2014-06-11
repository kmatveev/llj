package llj.asm.bytecode;

public class BiOpInstruction extends Instruction {

    public final TypeType type;
    public final Effect effect;

    public BiOpInstruction(InstructionCode code) {
        super(code);
        this.type = getType(code);
        effect = makeBiOp(type);
    }

    public static TypeType getType(InstructionCode code) {
        TypeType result;
        switch (code) {
            case dadd:
            case ddiv:
            case dmul:
            case drem:
            case dsub: result = TypeType.DOUBLE; break;

            case fadd:
            case fdiv:
            case fmul:
            case frem:
            case fsub: result = TypeType.FLOAT; break;

            case iadd:
            case iand:
            case idiv:
            case imul:
            case ior:
            case irem:
            case ishl:
            case ishr:
            case isub:
            case iushr:
            case ixor: result = TypeType.INT; break;

            case ladd:
            case land:
            case ldiv:
            case lmul:
            case lor:
            case lrem:
            case lshl:
            case lshr:
            case lsub:
            case lushr:
            case lxor: result = TypeType.LONG; break;

            default: throw new IllegalArgumentException();
        }
        return result;
    }

    public static Effect.Static makeBiOp(TypeType type) {
        return Effect.make().get(ScalarType.scalar(type)).get(ScalarType.scalar(type)).put(ScalarType.scalar(type));
        // return new Effect.Static(new Effect.Static.StackOp[] { new Effect.Static.StackOp(Type.scalar(type), Effect.Direction.GET), new Effect.Static.StackOp(Type.scalar(type), Effect.Direction.GET), new Effect.Static.StackOp(Type.scalar(type), Effect.Direction.PUT) });
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
