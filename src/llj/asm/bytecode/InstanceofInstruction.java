package llj.asm.bytecode;

public class InstanceofInstruction extends Instruction {

    public final ClassReference classRef;

    public final Effect effect;

    public InstanceofInstruction(InstructionCode code, ClassReference classRef) {
        super(code);
        this.classRef = classRef;
        Effect.Static stackEffect = Effect.make().get(null);
        if (code == InstructionCode._instanceof) {
            stackEffect = stackEffect.put(ScalarType.scalar(TypeType.BOOLEAN));
        } else if (code == InstructionCode.checkcast) {
            stackEffect = stackEffect.put(RefType.instanceRef(classRef));
        } else {
            throw new IllegalArgumentException();
        }
        this.effect = stackEffect;
    }

    @Override
    public String toString() {
        return super.toString() + " " + classRef.toString();
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
