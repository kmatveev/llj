package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.*;

public class StackInstruction extends Instruction {

    public boolean pushDirection;

    public Instruction.Operand.Reg16 reg16;

    public StackInstruction(boolean pushDirection, Operand.Reg16 reg16) {
        this.pushDirection = pushDirection;
        this.reg16 = reg16;
        getCode(pushDirection, reg16);
    }

    private StackInstruction(int code, int prefix1, boolean pushDirection, Operand.Reg16 reg16) {
        this.pushDirection = pushDirection;
        this.reg16 = reg16;
    }

    public void getCode(boolean pushDirection, Operand.Reg16 reg16) {
        int code, prefix1 = -1;
        if (reg16 == REG_BC) {
            code = 0xC1;
        } else if (reg16 == Operand.Reg16.REG_DE) {
            code = 0xD1;
        } else if (reg16 == Operand.Reg16.REG_HL) {
            code = 0xE1;
        } else if (reg16 == REG_IX) {
            prefix1 = IX_PREFIX;
            code = 0xE1;
        } else if (reg16 == REG_IY) {
            prefix1 = IY_PREFIX;
            code = 0xE1;
        } else if (reg16 == Operand.Reg16.REG_AF) {
            code = 0xF1;
        } else {
            throw new RuntimeException();
        }

        if (pushDirection) {
            code += 0x04;
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static StackInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0xC1) {
                return new StackInstruction(code, prefix1,false, REG_BC);
            } else if (code == 0xD1) {
                return new StackInstruction(code, prefix1,false, Operand.Reg16.REG_DE);
            } else if (code == 0xE1) {
                return new StackInstruction(code, prefix1,false, Operand.Reg16.REG_HL);
            } else if (code == 0xF1) {
                return new StackInstruction(code, prefix1,false, Operand.Reg16.REG_AF);
            } else if (code == 0xC5) {
                return new StackInstruction(code, prefix1,true, Operand.Reg16.REG_BC);
            } else if (code == 0xD5) {
                return new StackInstruction(code, prefix1,true, Operand.Reg16.REG_DE);
            } else if (code == 0xE5) {
                return new StackInstruction(code, prefix1,true, Operand.Reg16.REG_HL);
            } else if (code == 0xF5) {
                return new StackInstruction(code, prefix1,true, Operand.Reg16.REG_AF);
            } else {
                return null;
            }
        } else if (prefix1 == IX_PREFIX) {
            if (code == 0xE1) {
                return new StackInstruction(code, prefix1,false, REG_IX);
            } else if (code == 0xE5) {
                return new StackInstruction(code, prefix1,true, REG_IX);
            } else {
                return null;
            }
        } else if (prefix1 == IY_PREFIX) {
            if (code == 0xE1) {
                return new StackInstruction(code, prefix1,false, REG_IY);
            } else if (code == 0xE5) {
                return new StackInstruction(code, prefix1,true, REG_IY);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return pushDirection ? "PUSH" : "POP";
    }

    public String[] getAllMnemonics() {
        return new String[] {"PUSH", "POP"};
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
