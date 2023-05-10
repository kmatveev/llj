package llj.asm.z80;

public class FlagInstruction extends Instruction {

    public static enum Type{
        SCF, CCF
    }

    public final Type type;

    public FlagInstruction(Type type) {
        this.type = type;
        getCode(type);
    }

    private FlagInstruction(int code, Type type) {
        this.type = type;
        this.opCode = code;
    }

    private void getCode(Type type) {
        int code;
        if (type == Type.SCF) {
            code = 0x37;
        } else if (type == Type.CCF) {
            code = 0x3F;
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
    }

    public static FlagInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0x37) {
                return new FlagInstruction(code, Type.SCF);
            } else if (code == 0x3F) {
                return new FlagInstruction(code, Type.CCF);
            } else {
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
        return new String[] {"SCF", "CCF"};
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
