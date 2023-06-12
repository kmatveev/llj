package llj.asm.z80;

public class InterruptInstruction extends Instruction {

    public static enum Type {HALT, EI, DI, IM}

    public final Type type;
    public int im;

    public static final InterruptInstruction HALT = new InterruptInstruction(Type.HALT, 0);
    public static final InterruptInstruction EI = new InterruptInstruction(Type.EI, 0);
    public static final InterruptInstruction DI = new InterruptInstruction(Type.DI, 0);
    public static final InterruptInstruction IM_0 = new InterruptInstruction(Type.IM, 0);
    public static final InterruptInstruction IM_1 = new InterruptInstruction(Type.IM, 1);
    public static final InterruptInstruction IM_2 = new InterruptInstruction(Type.IM, 2);

    private InterruptInstruction(Type type, int im) {
        this.type = type;
        this.im = im;
        try {
            getCode(type, im);
        } catch (IncorrectOperandException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void getCode(Type type, int im) throws IncorrectOperandException {
        int code, prefix1 = -1;
        if (type == Type.HALT) {
            code = 0x76;
        } else if (type == Type.EI) {
            code = 0xFB;
        } else if (type == Type.DI) {
            code = 0xF3;
        } else if (type == Type.IM) {
            prefix1 = 0xED;
            if (im == 0) {
                code = 0x46;
            } else if (im == 1) {
                code = 0x56;
            } else if (im == 2) {
                code = 0x5E;
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static InterruptInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0x76) {
                return HALT;
            } else if (code == 0xFB) {
                return EI;
            } else if (code == 0xF3) {
                return DI;
            } else {
                return null;
            }
        } else if (prefix1 == 0xED) {
            if (code == 0x46) {
                return IM_0;
            } else if (code == 0x56) {
                return IM_1;
            } else if (code == 0x5E) {
                return IM_2;
            } else {
                // TODO support undocumented variants of IM0, IM1, IM2, and IM0/1
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return type.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"HALT", "EI", "DI", "IM"};
    }


    @Override
    public int decodeRemaining(byte[] data, int offset) {
        return offset;
    }

    @Override
    public int getSize() {
        return getOperationSize();
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        return offset;
    }
}
