package llj.asm.z80;

public class BlockInstruction extends Instruction {

    public static enum Operation {
        LD, CP, IN, OUT
    }

    public final Operation operation;
    public final boolean repeated;
    public final boolean increment;

    public BlockInstruction(Operation operation, boolean repeated, boolean increment) {
        this.operation = operation;
        this.repeated = repeated;
        this.increment = increment;
        getCode(operation, repeated, increment);
    }

    private BlockInstruction(int code, int prefix1, Operation operation, boolean repeated, boolean increment) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.operation = operation;
        this.repeated = repeated;
        this.increment = increment;
    }

    private void getCode(Operation operation, boolean repeated, boolean increment) {
        int prefix1 = 0xED;
        int code;
        if (operation == Operation.LD) {
            code = 0xA0;
        } else if (operation == Operation.CP) {
            code = 0xA1;
        } else if (operation == Operation.IN) {
            code = 0xA2;
        } else if (operation == Operation.OUT) {
            code = 0xA3;
        } else {
            throw new RuntimeException();
        }

        if (!increment) {
            code += 0x08;
        }

        if (repeated) {
            code += 0x10;
        }
        this.prefix1 = prefix1;
        this.opCode = code;
    }

    public BlockInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == 0xED) {
            if ((code >= 0xA0) && (code <= 0xBF)) {

                int c1 = code & 0x07;
                Operation operation;
                if (c1 == 0) {
                    operation = Operation.LD;
                } else if (c1 == 1) {
                    operation = Operation.CP;
                } else if (c1 == 2) {
                    operation = Operation.IN;
                } else if (c1 == 3) {
                    operation = Operation.OUT;
                } else {
                    throw new RuntimeException();
                }

                boolean repeated, increment;
                int c2 = code & 0x3F;
                if (c2 == 0x20) {
                    repeated = false;
                    increment = true;
                } else if (c2 == 0x28) {
                    repeated = false;
                    increment = false;
                } else if (c2 == 0x30) {
                    repeated = true;
                    increment = true;
                } else if (c2 == 0x38) {
                    repeated = true;
                    increment = false;
                } else {
                    throw new RuntimeException();
                }

                return new BlockInstruction(code, prefix1, operation, repeated, increment);

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        // TODO OTIR/OTDR instead of OUTIR/OUTDR
        return operation.name() + (increment ? "I" : "D") + (repeated ? "R" : "");
    }

    public String[] getAllMnemonics() {
        return new String[] {"LDI", "CPI", "INI", "OUTI", "LDD", "CPD", "IND", "OUTD"," LDIR", "CPIR", "INIR", "OTIR", "LDDR", "CPDR", "INDR", "OTDR" };
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
