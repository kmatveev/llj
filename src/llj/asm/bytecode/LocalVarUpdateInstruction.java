package llj.asm.bytecode;

public class LocalVarUpdateInstruction extends Instruction {

    public final int localVarA;
    public final Effect effect;

    public LocalVarUpdateInstruction(InstructionCode code, int localVarA, int update) {
        super(code);
        effect = Effect.make();
        this.localVarA = localVarA;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
