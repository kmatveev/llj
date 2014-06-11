package llj.asm.bytecode;

import java.util.EmptyStackException;

public abstract class Effect {

    public static Static make() {
        return new Static(new Static.StackOp[0]);
    }

    public abstract void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException;

    public static class Static extends Effect {

        private final StackOp[] components;

        public Static(StackOp[] components) {
            this.components = components;
        }

        private static void apply(OpStack opStack, Direction direction, Type operandType) throws IncompatibleStackEffectException, UnresolvedReference, ClassesNotLoadedException {
            if (direction == Direction.GET) {
                Type obtained;
                try {
                    obtained = opStack.content.pop();
                } catch (EmptyStackException e) {
                    throw new IncompatibleStackEffectException("OpStack Underflow", e);
                }
                if (operandType != null && !operandType.isAssignableFrom(obtained)) throw new IncompatibleStackEffectException();
            } else if (direction == Direction.PUT) {
                opStack.content.push(operandType);
            } else {
                throw new RuntimeException();
            }
        }

        private Static append(StackOp op) {

            if (op.direction == Direction.GET && components.length > 0) {
                StackOp prevOp = components[components.length - 1];
                if (prevOp.direction == Direction.PUT) {
                    throw new IllegalArgumentException("Wrong operation order: 'GET' operations cannot be appended to 'PUT' operations");
                }
            }

            StackOp[] ops = new StackOp[components.length + 1];
            System.arraycopy(components, 0, ops, 0, components.length);
            ops[ops.length - 1] = op;
            return new Static(ops);
        }

        public Static put(Type type) {
            return append(new StackOp(type, Direction.PUT));
        }

        public Static get(Type type) {
            return append(new StackOp(type, Direction.GET));
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, ClassesNotLoadedException{
            for (StackOp op : components) {
                apply(opStack, op);
            }
        }

        public static void apply(OpStack opStack, StackOp op) throws IncompatibleStackEffectException, ClassesNotLoadedException {
            apply(opStack, op.direction, op.operandType);
        }

        private enum Direction { PUT, GET }

        public static final class StackOp {

            public final Type operandType;
            public final Direction direction;

            public StackOp(Type operandType, Direction direction) {
                this.operandType = operandType;
                this.direction = direction;
            }

        }
    }

}
