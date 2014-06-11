package llj.asm.bytecode;

public class LoadConstInstruction extends Instruction {

    public final ConstantData constantData;

    public LoadConstInstruction(InstructionCode code, ConstantData constantData) {
        super(code);
        this.constantData = constantData;
    }

    public Effect getEffect() {
        return new LDCEffect();
    }

    @Override
    public String toString() {
        return code.name() + " " + constantData.toString();
    }

    class LDCEffect extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            opStack.content.push(constantData.type);
        }

    }

}
