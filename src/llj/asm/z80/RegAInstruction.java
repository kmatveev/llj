package llj.asm.z80;

public class RegAInstruction extends Instruction {

    public enum Type {
        DAA, CPL, NEG
    }

    public final Type type;

    public RegAInstruction(Type type) {
        this.type = type;
        getCode(type);
    }

    private RegAInstruction(int code, int prefix1, Type type) {
        this.type = type;
        this.opCode = code;
    }

    private void getCode(Type type) {
        int code;
        int prefix1 = -1;
        if (type == Type.DAA) {
            code = 0x27;
        } else if (type == Type.CPL) {
            code = 0x2F;
        } else if (type == Type.NEG) {
            prefix1 = 0xED;
            code = 0x44;
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static RegAInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0x27) {
                return new RegAInstruction(code, prefix1, Type.DAA);
            } else if (code == 0x2F) {
                return new RegAInstruction(code, prefix1, Type.CPL);
            } else {
                return null;
            }
        } else if ((prefix1 == 0xED) && ((code == 0x44) || (code == 0x54) || (code == 0x64) || (code == 0x74) || (code == 0x4C) || (code == 0x5C) || (code == 0x6C) || (code == 0x7C))) {
            return new RegAInstruction(code, prefix1, Type.NEG);
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return type.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"DAA", "CPL", "NEG"};
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
