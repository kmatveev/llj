package llj.asm.bytecode;

public class RetInstruction extends Instruction {

    public final Effect effect;

    public RetInstruction() {
        super(InstructionCode.ret);
        effect = Effect.make();
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
