package llj.asm.bytecode;

public class LoadImmediateInstruction extends Instruction {

    public final TypeType type;
    public final short val;

    public LoadImmediateInstruction(InstructionCode code, short val) {
        super(code);
        this.type = getType(code);
        this.val = val;
    }

    public static TypeType getType(InstructionCode code) {
        TypeType type;
        switch (code) {
            case aconst_null:
                type = TypeType.REF; break;
            case iconst_0:
            case iconst_1:
            case iconst_2:
            case iconst_3:
            case iconst_4:
            case iconst_5:
            case iconst_m1:
                type = TypeType.INT; break;
            case lconst_0:
            case lconst_1:
                type = TypeType.LONG; break;
            case fconst_0:
            case fconst_1:
            case fconst_2:
                type = TypeType.FLOAT; break;
            case dconst_0:
            case dconst_1:
                type = TypeType.DOUBLE; break;
            case bipush:
                type = TypeType.INT; break;
            case sipush:
                type = TypeType.INT; break;
            default:
                throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        return type;
    }

    public LoadImmediateInstruction(TypeType type, short val) {
        super(getCode(type, val));
        this.type = type;
        this.val = val;
    }

    public static InstructionCode getCode(TypeType type, short val) {
        if (type == TypeType.INT) {
            if (val == -1) {
                return InstructionCode.iconst_m1;
            } else if (val == 0) {
                return InstructionCode.iconst_0;
            } else if (val == 1) {
                return InstructionCode.iconst_1;
            } else if (val == 2) {
                return InstructionCode.iconst_2;
            } else if (val == 3) {
                return InstructionCode.iconst_3;
            } else if (val == 4) {
                return InstructionCode.iconst_4;
            } else if (val == 5) {
                return InstructionCode.iconst_5;
            } else if (val <= 127 && val >= -128) {
                return InstructionCode.bipush;
            } else if (val <= 32767 && val >= -32768) {
                return InstructionCode.sipush;
            } else {
                throw new IllegalArgumentException();
            }
        } else if (type == TypeType.LONG) {
            if (val == 0) {
                return InstructionCode.lconst_0;
            } else if (val == 1) {
                return InstructionCode.lconst_1;
            } else {
                throw new IllegalArgumentException();
            }
        } else if (type == TypeType.FLOAT) {
            if (val == 0) {
                return InstructionCode.fconst_0;
            } else if (val == 1) {
                return InstructionCode.fconst_1;
            } else if (val == 2) {
                return InstructionCode.fconst_2;
            } else {
                throw new IllegalArgumentException();
            }
        } else if (type == TypeType.DOUBLE) {
            if (val == 0) {
                return InstructionCode.dconst_0;
            } else if (val == 1) {
                return InstructionCode.dconst_1;
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public short getIntValue() {
        short result;
        switch (code) {
            case iconst_0:
                result = 0; break;
            case iconst_1:
                result = 1; break;
            case iconst_2:
                result = 2; break;
            case iconst_3:
                result = 3; break;
            case iconst_4:
                result = 4; break;
            case iconst_5:
                result = 5; break;
            case iconst_m1:
                result = -1; break;
            case bipush:
            case sipush:
                result = val; break;
            default:
                throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        return result;
    }

    public short getLongValue() {
        short result;
        switch (code) {
            case lconst_0:
                result = 0; break;
            case lconst_1:
                result = 1; break;
            default:
                throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        return result;
    }

    public short getFloatValue() {
        short result;
        switch (code) {
            case fconst_0:
                result = 0; break;
            case fconst_1:
                result = 1; break;
            case fconst_2:
                result = 1; break;
            default:
                throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        return result;
    }

    public short getDoubleValue() {
        short result;
        switch (code) {
            case dconst_0:
                result = 0; break;
            case dconst_1:
                result = 1; break;
            default:
                throw new IllegalArgumentException("Wrong instruction code:" + code);
        }
        return result;
    }

    @Override
    public Effect getEffect() {
        return new LDIEffect();
    }

    class LDIEffect extends Effect {
        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException {
            opStack.content.push(ScalarType.scalar(type));
        }

    }

}
