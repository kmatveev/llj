package llj.asm.bytecode;

public class TypeCastInstruction extends Instruction {

    public final TypeType from, to;
    public final Effect effect;

    public TypeCastInstruction(InstructionCode code) {
        super(code);
        this.from = getFromType(code);
        this.to = getToType(code);
        effect = makeConvert(from, to);
    }

    public TypeCastInstruction(TypeType from, TypeType to) {
        super(makeCode(from, to));
        this.from = getFromType(code);
        this.to = getToType(code);
        effect = makeConvert(from, to);
    }

    public static TypeType getFromType(InstructionCode code) {

        TypeType result;
        switch (code) {
            case d2f:
            case d2i:
            case d2l: result = TypeType.DOUBLE; break;
            case f2d:
            case f2i:
            case f2l: result = TypeType.FLOAT; break;
            case i2b:
            case i2c:
            case i2d:
            case i2f:
            case i2l:
            case i2s: result = TypeType.INT; break;
            case l2d:
            case l2f:
            case l2i: result = TypeType.LONG; break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    public static TypeType getToType(InstructionCode code) {
        TypeType result;
        switch (code) {
            case d2f:
            case i2f:
            case l2f: result = TypeType.FLOAT; break;
            case d2i:
            case f2i:
            case l2i: result = TypeType.INT; break;
            case f2d:
            case l2d:
            case i2d: result = TypeType.DOUBLE; break;
            case f2l:
            case d2l:
            case i2l: result = TypeType.LONG; break;
            case i2b: result = TypeType.BYTE; break;
            case i2c: result = TypeType.CHAR; break;
            case i2s: result = TypeType.SHORT; break;
            default: throw new IllegalArgumentException();
        }
        return result;

    }

    public static InstructionCode makeCode(TypeType from, TypeType to) {
        switch (from) {
            case INT:
                switch (to) {
                    case BYTE:   return InstructionCode.i2b;
                    case CHAR:   return InstructionCode.i2c;
                    case SHORT:  return InstructionCode.i2s;
                    case LONG:   return InstructionCode.i2l;
                    case FLOAT:  return InstructionCode.i2f;
                    case DOUBLE: return InstructionCode.i2d;
                    default: throw new IllegalArgumentException();
                }
            case LONG:
                switch (to) {
                    case INT:    return InstructionCode.l2i;
                    case FLOAT:  return InstructionCode.l2f;
                    case DOUBLE: return InstructionCode.l2d;
                    default: throw new IllegalArgumentException();
                }
            case DOUBLE:
                switch (to) {
                    case LONG:   return InstructionCode.d2l;
                    case INT:    return InstructionCode.d2i;
                    case FLOAT:  return InstructionCode.d2f;
                    default: throw new IllegalArgumentException();
                }
            case FLOAT:
                switch (to) {
                    case LONG:   return InstructionCode.f2l;
                    case INT:    return InstructionCode.f2i;
                    case DOUBLE: return InstructionCode.f2d;
                    default: throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Effect.Static makeConvert(TypeType from, TypeType to) {
        return Effect.make().get(ScalarType.scalar(from)).put(ScalarType.scalar(to));
        // return new Effect.Static(new Effect.Static.StackOp[] { new Effect.Static.StackOp(Type.scalar(from), Effect.Direction.GET), new Effect.Static.StackOp(Type.scalar(to), Effect.Direction.PUT) });
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
