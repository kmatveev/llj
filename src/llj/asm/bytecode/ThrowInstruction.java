package llj.asm.bytecode;

public class ThrowInstruction extends Instruction {

    public Effect effect;

    public ThrowInstruction() {
        super(InstructionCode.athrow);
        effect = Effect.make().get(RefType.instanceRef(ClassIntrinsics.TROWABLE_CLASS_REF));
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
