package llj.asm.bytecode;

import java.util.EmptyStackException;

public class ArrayAccessInstruction extends Instruction {

    public final TypeType elementType;
    public final boolean load;
    public final Effect effect;

    public ArrayAccessInstruction(InstructionCode code) {
        super(code);

        switch (code) {
            case aaload:  elementType = TypeType.REF;    load = true;  break;
            case aastore: elementType = TypeType.REF;    load = false; break;
            case daload:  elementType = TypeType.DOUBLE; load = true;  break;
            case dastore: elementType = TypeType.DOUBLE; load = false; break;
            case faload:  elementType = TypeType.FLOAT;  load = true;  break;
            case fastore: elementType = TypeType.FLOAT;  load = false; break;
            case iaload:  elementType = TypeType.INT;    load = true;  break;
            case iastore: elementType = TypeType.INT;    load = false; break;
            case laload:  elementType = TypeType.LONG;   load = true;  break;
            case lastore: elementType = TypeType.LONG;   load = false; break;
            case baload:  elementType = TypeType.BOOLEAN;load = true;  break;
            case bastore: elementType = TypeType.BOOLEAN;load = false; break;
            case caload:  elementType = TypeType.CHAR;   load = true;  break;
            case castore: elementType = TypeType.CHAR;   load = false; break;
            case saload:  elementType = TypeType.SHORT;  load = true;  break;
            case sastore: elementType = TypeType.SHORT;  load = false; break;
            default: throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        this.effect = load ? new ALoadEffect(elementType) : new AStoreEffect(elementType);
    }

    public ArrayAccessInstruction(boolean load, TypeType type) {
        super(makeCode(load, type));
        this.load = load;
        this.elementType = type;
        this.effect = load ? new ALoadEffect(type) : new AStoreEffect(type);
    }

    public static InstructionCode makeCode(boolean load, TypeType type) {
        switch (type) {
            case BOOLEAN:
            case BYTE:
                return load ? InstructionCode.baload : InstructionCode.bastore;
            case CHAR:
                return load ? InstructionCode.caload : InstructionCode.castore;
            case SHORT:
                return load ? InstructionCode.saload : InstructionCode.sastore;
            case INT:
                return load ? InstructionCode.iaload : InstructionCode.iastore;
            case FLOAT:
                return load ? InstructionCode.faload : InstructionCode.fastore;
            case DOUBLE:
                return load ? InstructionCode.daload : InstructionCode.dastore;
            case LONG:
                return load ? InstructionCode.laload : InstructionCode.lastore;
            case REF:
                return load ? InstructionCode.aaload : InstructionCode.aastore;
            default:
                throw new IllegalArgumentException("Wrong type code:" + type);
        }
    }

    private static class ALoadEffect extends Effect {

        private final TypeType type;

        private ALoadEffect(TypeType type) {
            this.type = type;
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException {

            Type indexType;
            try {
                indexType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!ScalarType.scalar(TypeType.INT).isAssignableFrom(indexType)) throw new IncompatibleStackEffectException();

            Type valueType;
            try {
                valueType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }

            if (valueType.type != TypeType.ARRAY_REF) throw new IncompatibleStackEffectException();
            ArrayRefType arrayRefType = (ArrayRefType)valueType;
            if (arrayRefType.elemType.type != this.type) throw new IncompatibleStackEffectException();

            opStack.content.push(arrayRefType.elemType);

        }

    }

    private static class AStoreEffect extends Effect {

        private final TypeType type;

        private AStoreEffect(TypeType type) {
            this.type = type;
        }

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException {

            Type itemType;
            try {
                itemType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }

            Type indexType;
            try {
                indexType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (!ScalarType.scalar(TypeType.INT).isAssignableFrom(indexType)) throw new IncompatibleStackEffectException();

            Type valueType;
            try {
                valueType = opStack.content.pop();
            } catch (EmptyStackException e) {
                throw new IncompatibleStackEffectException("OpStack Underflow", e);
            }
            if (valueType.type != TypeType.ARRAY_REF) throw new IncompatibleStackEffectException();
            ArrayRefType arrayRefType = (ArrayRefType)valueType;
            if (arrayRefType.elemType.type != this.type) throw new IncompatibleStackEffectException();

            if (!itemType.isAssignableFrom(arrayRefType.elemType)) throw new IncompatibleStackEffectException();

        }

    }

//    public static Effect.Static makeALoad(TypeType type) {
//        return new Effect.Static(
//                new Effect.Static.StackOp[] {
//                        new Effect.Static.StackOp(TypeType.ARRAY_REF, Effect.Direction.GET),
//                        new Effect.Static.StackOp(TypeType.INT, Effect.Direction.GET),
//                        new Effect.Static.StackOp(type, Effect.Direction.PUT)
//                });
//    }
//
//    public static Effect.Static makeAStore(TypeType type) {
//        return new Effect.Static(
//                new Effect.Static.StackOp[] {
//                        new Effect.Static.StackOp(TypeType.ARRAY_REF, Effect.Direction.GET),
//                        new Effect.Static.StackOp(TypeType.INT, Effect.Direction.GET),
//                        new Effect.Static.StackOp(type, Effect.Direction.GET)
//                });
//    }

    @Override
    public String toString() {
        return code.name() + " ";
    }

    public Effect getEffect() {
        return effect;
    }


}
