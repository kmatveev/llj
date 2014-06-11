package llj.asm.bytecode;

public class NewInstanceInstruction extends Instruction {

    public final ClassReference classRef;

    public final Effect effect;

    public NewInstanceInstruction(InstructionCode code, ClassReference classRef) {
        super(code);
        this.classRef = classRef;
        this.effect = Effect.make().put(RefType.instanceRef(classRef));
    }

    @Override
    public String toString() {
        return code.name() + " " + classRef.toString();
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
