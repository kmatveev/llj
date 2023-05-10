package llj.asm.z80;

public class DjnzInstruction extends ControlTransferInstruction {

    public int offset;

    public DjnzInstruction(int offset) {
        this.offset = offset;
        this.opCode = 0x10;
    }

    private DjnzInstruction(int offset, boolean partial) {
        this.offset = offset;
        this.opCode = 0x10;
    }

    public static DjnzInstruction decodeInitial(int code, int prefix1) {
        if ((prefix1 == -1) && (code == 0x10)) {
            return new DjnzInstruction(0, true);
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "DJNZ";
    }

    public String[] getAllMnemonics() {
        return new String[] {"DJNZ"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        this.offset = data[offset];
        offset++;
        return offset;
    }

    @Override
    public int getSize() {
        return getOperationSize() + 1;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        dest[offset] = (byte)this.offset;
        offset++;
        return offset;
    }
}
