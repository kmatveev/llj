package llj.asm.bytecode;

public class NewArrayInstruction extends Instruction {

    public final Type arrayElemType;
    public final int dimensions;
    public final Effect effect;

    public NewArrayInstruction(ArrayElemType arrayElemType) {
        super(InstructionCode.newarray);
        dimensions = 1;
        this.arrayElemType = ScalarType.scalar(arrayElemType.type);
        effect = Effect.make().get(ScalarType.scalar(TypeType.INT)).put(ArrayRefType.arrayOf(this.arrayElemType));
    }

    public NewArrayInstruction(InstructionCode code, ClassReference arrayElemType, int dimensions) {
        super(code);
        this.arrayElemType = RefType.instanceRef(arrayElemType);
        if (code == InstructionCode.anewarray) {
            effect = Effect.make().get(ScalarType.scalar(TypeType.INT)).put(ArrayRefType.arrayOf(this.arrayElemType));
            this.dimensions = 1;
        } else if (code == InstructionCode.multianewarray) {
            this.dimensions = dimensions;
            Effect.Static stackEffect = Effect.make();
            for (int i = 0; i < dimensions; i++) {
                stackEffect = stackEffect.get(ScalarType.scalar(TypeType.INT));
            }
            stackEffect = stackEffect.put(ArrayRefType.arrayOf(this.arrayElemType));
            this.effect = stackEffect;
        } else {
            throw new RuntimeException();
        }
    }

    protected NewArrayInstruction(InstructionCode code, Type arrayElemType, int dimensions, Effect effect) {
        super(code);
        this.arrayElemType = arrayElemType;
        this.dimensions = dimensions;
        this.effect = effect;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
