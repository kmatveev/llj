package llj.asm.z80;

public class RetInstruction extends ControlTransferInstruction {

    public static enum InterruptType { I, NMI };

    public final Condition condition;
    public final InterruptType interruptType;

    private RetInstruction(Condition condition) throws IncorrectOperandException {
        this.condition = condition;
        this.interruptType = null;
        getCode(condition, null);
    }

    private RetInstruction(InterruptType interruptType) throws IncorrectOperandException {
        this.condition = null;
        this.interruptType = interruptType;
        getCode(null, interruptType);
    }

    private RetInstruction(int code, int prefix1, Condition condition, InterruptType interruptType) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.condition = condition;
        this.interruptType = interruptType;
    }

    public void getCode(Condition condition, InterruptType interruptType) throws IncorrectOperandException {
        int code, prefix1 = -1;
        if (condition == null) {
            if (interruptType == null) {
                code = 0xC9;
            } else if (interruptType == InterruptType.I) {
                prefix1 = 0xED;
                code = 0x4D;
            } else if (interruptType == InterruptType.NMI) {
                prefix1 = 0xED;
                code = 0x45;
            } else {
                throw new RuntimeException();
            }
        } else {
            if (interruptType != null) {
                throw new IncorrectOperandException();
            }

            if (condition.flag == ConditionFlag.Z) {
                code = 0xC0;
            } else if (condition.flag == ConditionFlag.C) {
                code = 0xD0;
            } else if (condition.flag == ConditionFlag.PV) {
                code = 0xE0;
            } else if (condition.flag == ConditionFlag.S) {
                code = 0xF0;
            } else {
                throw new RuntimeException();
            }

            if (condition.set) {
                code += 0x08;
            }
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static RetInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0xC9) {
                return new RetInstruction(code, prefix1, null, null);
            } else if (code == 0xC0) {
                return new RetInstruction(code, prefix1, Condition.NZ, null);
            } else if (code == 0xD0) {
                return new RetInstruction(code, prefix1, Condition.NC, null);
            } else if (code == 0xE0) {
                return new RetInstruction(code, prefix1, Condition.PO, null);
            } else if (code == 0xF0) {
                return new RetInstruction(code, prefix1, Condition.P, null);
            } else if (code == 0xC8) {
                return new RetInstruction(code, prefix1, Condition.Z, null);
            } else if (code == 0xD8) {
                return new RetInstruction(code, prefix1, Condition.C, null);
            } else if (code == 0xE8) {
                return new RetInstruction(code, prefix1, Condition.PE, null);
            } else if (code == 0xF8) {
                return new RetInstruction(code, prefix1, Condition.M, null);
            } else {
                return null;
            }
        } else if (prefix1 == 0xED){
            if (code == 0x4D) { // TODO more undocumented codes
                return new RetInstruction(code, prefix1, null, InterruptType.I);
            } else if (code == 0x45) { // TODO more undocumented codes
                return new RetInstruction(code, prefix1, null, InterruptType.NMI);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "RET " + ((condition != null) ? condition.toString() : "");
    }

    public String[] getAllMnemonics() {
        return new String[] {"RET", "RETI", "RETN"};
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
