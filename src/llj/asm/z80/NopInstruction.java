package llj.asm.z80;

public class NopInstruction extends Instruction {

    public static final NopInstruction NOP = new NopInstruction();

    private NopInstruction() {
        this.opCode = 0x00;
    }

    public static NopInstruction decode(int code, int prefix1) {
        if ((prefix1 == -1 ) && (code == 0x00)) {
            return NOP;
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "NOP";
    }

    public String[] getAllMnemonics() {
        return new String[] {"NOP" };
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
