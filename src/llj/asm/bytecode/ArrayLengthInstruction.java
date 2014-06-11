package llj.asm.bytecode;

public class ArrayLengthInstruction extends Instruction {

    public final Effect effect;

    public ArrayLengthInstruction() {
        super(InstructionCode.arraylength);
        effect = Effect.make().get(ArrayRefType.anyArray()).put(ScalarType.scalar(TypeType.INT));
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
