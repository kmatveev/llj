package llj.asm.bytecode;

import java.util.EmptyStackException;

public class StackInstruction extends Instruction {

    private final Effect effect;

    public StackInstruction(InstructionCode code) {
        super(code);
        Effect effect;
        switch (code) {
            case dup:     effect = new Dup(1); break;
            case dup_x1:  effect = new DupX1(1); break;
            case dup_x2:  effect = new DupX2(1); break;
            case dup2:    effect = new Dup(2); break;
            case dup2_x1: effect = new DupX1(2); break;
            case dup2_x2: effect = new DupX2(2); break;
            case nop:     effect = Effect.make(); break;
            case pop:     effect = Effect.make().get(null); break;
            case pop2:    effect = Effect.make().get(null).get(null); break;
            case swap:    effect = new Swap(); break;
            default: throw new IllegalArgumentException();
        }
        this.effect = effect;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }

    public static class Swap extends Effect {

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            Type topType;
            try {
                topType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            Type secondType;
            try {
                secondType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (TypeType.size(topType.type) != 1 || TypeType.size(secondType.type) != 1) throw new IncompatibleStackEffectException();
            opStack.content.push(topType);
            opStack.content.push(secondType);
        }

    }

    public static class Dup extends Effect {

        private final int size;

        public Dup(int size) {
            this.size = size;
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            Type topType;
            try {
                topType = opStack.content.peek();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (TypeType.size(topType.type) != size) throw new IncompatibleStackEffectException();
            opStack.content.push(topType);
        }

    }

    public static class DupX1 extends Effect {

        private final int size;

        public DupX1(int size) {
            this.size = size;
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            Type topType;
            try {
                topType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            Type secondType;
            try {
                secondType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (TypeType.size(topType.type) != size) throw new IncompatibleStackEffectException();
            opStack.content.push(topType);
            opStack.content.push(secondType);
            opStack.content.push(topType);
        }

    }

    public static class DupX2 extends Effect {

        private final int size;

        public DupX2(int size) {
            this.size = size;
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            Type topType;
            try {
                topType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            Type secondType;
            try {
                secondType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            Type thirdType;
            try {
                thirdType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (TypeType.size(topType.type) != size) throw new IncompatibleStackEffectException();
            opStack.content.push(topType);
            opStack.content.push(thirdType);
            opStack.content.push(secondType);
            opStack.content.push(topType);
        }

    }

}
