package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.*;

public class ExInstruction extends Instruction {

    public Operand op1, op2;

    public ExInstruction(Operand op1, Operand op2) throws IncorrectOperandException{
        this.op1 = op1;
        this.op2 = op2;
        getCode(op1, op2);
    }

    private ExInstruction(int code, int prefix1, Operand op1, Operand op2) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.op1 = op1;
        this.op2 = op2;
    }

    public void getCode(Operand op1, Operand op2) throws IncorrectOperandException {
        int code, prefix1 = -1;
        if ((op1.type == Operand.Type.MEM_PTR_REG) && (op1.reg16 == REG_SP) && (op2.type == Operand.Type.REG16)) {
            if (op2.reg16 == REG_HL) {
                code = 0xE3;
            } else if (op2.reg16 == REG_IX) {
                prefix1 = IX_PREFIX;
                code = 0xE3;
            } else if (op2.reg16 == REG_IY) {
                prefix1 = IY_PREFIX;
                code = 0xE3;
            } else {
                throw new IncorrectOperandException();
            }
        } else if ((op1.type == Operand.Type.REG16) && (op1.reg16 == REG_DE) && (op2.type == Operand.Type.REG16) && (op2.reg16 == REG_HL)) {
            code = 0xEB;
        } else {
            throw new IncorrectOperandException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static ExInstruction decodeInitial(int code, int prefix1) {
        if ((prefix1 == -1) && (code == 0xE3)) {
            return new ExInstruction(code, prefix1, Operand.memRegPtr(Operand.Reg16.REG_SP), Operand.reg16(REG_HL));
        } else if ((prefix1 == IX_PREFIX) && (code == 0xE3)) {
            return new ExInstruction(code, prefix1, Operand.memRegPtr(Operand.Reg16.REG_SP), Operand.reg16(REG_IX));
        } else if ((prefix1 == IY_PREFIX) && (code == 0xE3)) {
            return new ExInstruction(code, prefix1, Operand.memRegPtr(Operand.Reg16.REG_SP), Operand.reg16(REG_IY));
        } else if ((prefix1 == -1) && (code == 0xEB)) {
            return new ExInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_DE), Operand.reg16(REG_HL));
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return "EX";
    }

    public String[] getAllMnemonics() {
        return new String[] {"EX"};
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
