package llj.asm.bytecode;

public class MonitorInstruction extends Instruction {

    public final boolean enter;
    public final Effect effect;

    public MonitorInstruction(InstructionCode code) {
        super(code);
        if (code == InstructionCode.monitorenter) {
            enter = true;
        } else if (code == InstructionCode.monitorexit) {
            enter = false;
        } else {
            throw new IllegalArgumentException();
        }
        effect = Effect.make().get(RefType.anyRef());
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
