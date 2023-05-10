package llj.asm.z80;

public class ComplexRotateInstruction extends Instruction {

    public static enum Type {
        RLD, RRD
    }

    public final Type type;

    public ComplexRotateInstruction(Type type) {
        this.type = type;
        getCode(type);
    }

    private ComplexRotateInstruction(int code, int prefix1, Type type) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.type = type;
    }

    public void getCode(Type type) {
        int code;
        int prefix1 = 0xED;
        if (type == Type.RLD) {
            code = 0x6F;
        } else if (type == Type.RRD) {
            code = 0x67;
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static ComplexRotateInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == 0xED) {
            if (code == 0x6F) {
                return new ComplexRotateInstruction(code, prefix1, Type.RLD);
            } else if (code == 0x67) {
                return new ComplexRotateInstruction(code, prefix1, Type.RRD);
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
        return new String[] {"RLD", "RRD"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        return 0;
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
