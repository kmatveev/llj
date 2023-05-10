package llj.asm.z80;

public class RstInstruction extends ControlTransferInstruction {

    public int absAddr;

    public static final RstInstruction RST_0x00 = new RstInstruction(0x00);
    public static final RstInstruction RST_0x08 = new RstInstruction(0x08);
    public static final RstInstruction RST_0x10 = new RstInstruction(0x10);
    public static final RstInstruction RST_0x18 = new RstInstruction(0x18);
    public static final RstInstruction RST_0x20 = new RstInstruction(0x20);
    public static final RstInstruction RST_0x28 = new RstInstruction(0x28);
    public static final RstInstruction RST_0x30 = new RstInstruction(0x30);
    public static final RstInstruction RST_0x38 = new RstInstruction(0x38);

    private RstInstruction(int absAddr) {
        this.absAddr = absAddr;
        try {
            getCode(absAddr);
        } catch (IncorrectOperandException ex) {
            throw new RuntimeException(ex);
        }
    }

    private RstInstruction(int code, int prefix1, int absAddr) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.absAddr = absAddr;
    }

    public void getCode(int absAddr) throws IncorrectOperandException {
        int code;
        if (absAddr == 0x00) {
            code = 0xC7;
        } else if (absAddr == 0x08) {
            code = 0xCF;
        } else if (absAddr == 0x10) {
            code = 0xD7;
        } else if (absAddr == 0x18) {
            code = 0xDF;
        } else if (absAddr == 0x20) {
            code = 0xE7;
        } else if (absAddr == 0x28) {
            code = 0xEF;
        } else if (absAddr == 0x30) {
            code = 0xF7;
        } else if (absAddr == 0x38) {
            code = 0xFF;
        } else {
            throw new IncorrectOperandException();
        }

        this.opCode = code;
    }

    public static RstInstruction decodeInitial(int code, int prefix1) {
        if (code == 0xC7) {
            return RST_0x00;
        } else if (code == 0xCF) {
            return RST_0x08;
        } else if (code == 0xD7) {
            return RST_0x10;
        } else if (code == 0xDF) {
            return RST_0x18;
        } else if (code == 0xE7) {
            return RST_0x20;
        } else if (code == 0xEF) {
            return RST_0x28;
        } else if (code == 0xF7) {
            return RST_0x30;
        } else if (code == 0xFF) {
            return RST_0x38;
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "RST";
    }

    public String[] getAllMnemonics() {
        return new String[] {"RST"};
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
