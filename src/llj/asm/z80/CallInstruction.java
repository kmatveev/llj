package llj.asm.z80;

public class CallInstruction extends ControlTransferInstruction {

    public int absAddr;
    public Condition condition;

    private CallInstruction(int val, Condition condition) throws IncorrectOperandException {
        this.absAddr = val;
        this.condition = condition;
        getCode(condition);
    }

    private CallInstruction(int code, int val, Condition condition) {
        this.opCode = code;
        this.absAddr = val;
        this.condition = condition;
    }

    public void getCode(Condition condition) {
        int code;
        if (condition == null) {
            code = 0xCD;
        } else {
            if (condition.flag == ConditionFlag.Z) {
                code = 0xC4;
            } else if (condition.flag == ConditionFlag.C) {
                code = 0xD4;
            } else if (condition.flag == ConditionFlag.PV) {
                code = 0xE4;
            } else if (condition.flag == ConditionFlag.S) {
                code = 0xF4;
            } else {
                throw new RuntimeException();
            }

            if (condition.set) {
                code += 0x08;
            }
        }
        this.opCode = code;
    }

    public static CallInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 != -1) {
            return null;
        }

        if (code == 0xCD) {
            return new CallInstruction(code, 0, null);
        } else if (code == 0xC4) {
            return new CallInstruction(code, 0, Condition.NZ);
        } else if (code == 0xD4) {
            return new CallInstruction(code, 0, Condition.NC);
        } else if (code == 0xE4) {
            return new CallInstruction(code,  0, Condition.PO);
        } else if (code == 0xF4) {
            return new CallInstruction(code, 0, Condition.P);
        } else if (code == 0xCC) {
            return new CallInstruction(code, 0, Condition.Z);
        } else if (code == 0xDC) {
            return new CallInstruction(code, 0, Condition.C);
        } else if (code == 0xEC) {
            return new CallInstruction(code, 0, Condition.PE);
        } else if (code == 0xFC) {
            return new CallInstruction(code, 0, Condition.M);
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "CALL " + ((condition != null) ? condition.toString() : "");
    }

    public String[] getAllMnemonics() {
        return new String[] {"CALL"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        absAddr = decodeImm16(data, offset);
        return offset + 2;
    }

    @Override
    public int getSize() {
        return getOperationSize() + 2;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        offset = encodeImm16(dest, offset, absAddr);
        return offset;
    }
}
