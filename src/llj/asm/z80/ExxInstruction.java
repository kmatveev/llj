package llj.asm.z80;

public class ExxInstruction extends Instruction {

    public static enum Type{
        EX_AF_AFF, EXX
    }

    public final Type type;

    public ExxInstruction(Type type) {
        this.type = type;
        getCode(type);
    }

    private ExxInstruction(int code, Type type) {
        this.type = type;
        this.opCode = code;
    }

    private void getCode(Type type) {
        int code;
        if (type == Type.EX_AF_AFF) {
            code = 0x08;
        } else if (type == Type.EXX) {
            code = 0xD9;
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
    }

    public static ExxInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0x08) {
                return new ExxInstruction(code, Type.EX_AF_AFF);
            } else if (code == 0xD9) {
                return new ExxInstruction(code, Type.EXX);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return type == Type.EX_AF_AFF ? "EX" : "EXX";
    }

    public String[] getAllMnemonics() {
        return new String[] {"EX", "EXX"};
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

