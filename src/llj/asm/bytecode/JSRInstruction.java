package llj.asm.bytecode;

public class JSRInstruction extends Instruction {

    public final Effect effect;
    public final int location;

    public JSRInstruction(InstructionCode code, int location) {
        super(code);
        this.location = location;
        effect = Effect.make().put(ScalarType.scalar(TypeType.ADDRESS));
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
