package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg8.REG_A;
import static llj.asm.z80.Instruction.Operand.Reg8.REG_B;

public class IoInstruction extends Instruction {

    public boolean in;
    public Operand op1, op2;

    public IoInstruction(boolean in, Operand op1, Operand op2) throws IncorrectOperandException{
        this.in = in;
        this.op1 = op1;
        this.op2 = op2;
        getCode(in, op1, op2);
    }

    private IoInstruction(int code, int prefix1, boolean in, Operand op1, Operand op2, boolean partial) {
        this.opCode= code;
        this.prefix1 = prefix1;
        this.in = in;
        this.op1 = op1;
        this.op2 = op2;
    }


    public void getCode(boolean in, Operand op1, Operand op2) throws IncorrectOperandException {
        int code, prefix1;
        if (in) {
            if (op2.type == Operand.Type.PORT_PTR_IMM8) {
                if ((op1.type == Operand.Type.REG8) && (op1.reg8 == REG_A)) {
                    code = 0xDB;
                    prefix1 = -1;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if ((op2.type == Operand.Type.MEM_PTR_REG) && (op2.reg16 == Operand.Reg16.REG_BC) && ((op1.type == Operand.Type.REG8) )) {
                prefix1 = 0xED;
                if (op1.reg8 == REG_B) {
                    code = 0x40;
                } else if (op1.reg8 == Operand.Reg8.REG_C) {
                    code = 0x48;
                } else if (op1.reg8 == Operand.Reg8.REG_D) {
                    code = 0x50;
                } else if (op1.reg8 == Operand.Reg8.REG_E) {
                    code = 0x58;
                } else if (op1.reg8 == Operand.Reg8.REG_H) {
                    code = 0x60;
                } else if (op1.reg8 == Operand.Reg8.REG_L) {
                    code = 0x68;
                } else if (op1.reg8 == REG_A) {
                    code = 0x78;
                } else if (op1.reg8 == null) {
                    code = 0x70; // undocumented
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            if (op1.type == Operand.Type.PORT_PTR_IMM8) {
                if ((op2.type == Operand.Type.REG8) && (op2.reg8 == REG_A)) {
                    code = 0xD3;
                    prefix1 = -1;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if ((op1.type == Operand.Type.MEM_PTR_REG) && (op1.reg16 == Operand.Reg16.REG_BC) && ((op2.type == Operand.Type.REG8) )) {
                prefix1 = 0xED;
                if (op2.reg8 == REG_B) {
                    code = 0x41;
                } else if (op2.reg8 == Operand.Reg8.REG_C) {
                    code = 0x49;
                } else if (op2.reg8 == Operand.Reg8.REG_D) {
                    code = 0x51;
                } else if (op2.reg8 == Operand.Reg8.REG_E) {
                    code = 0x59;
                } else if (op2.reg8 == Operand.Reg8.REG_H) {
                    code = 0x61;
                } else if (op2.reg8 == Operand.Reg8.REG_L) {
                    code = 0x69;
                } else if (op2.reg8 == REG_A) {
                    code = 0x79;
                } else if (op2.reg8 == null) {
                    code = 0x71; // undocumented
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new IncorrectOperandException();
            }

        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static IoInstruction decodeInitial(int code, int prefix1) {
        if (prefix1 == -1) {
            if (code == 0xDB) {
                return new IoInstruction(code, prefix1, true, Operand.reg8(REG_A), Operand.port(0), true);
            } else if (code == 0xD3) {
                return new IoInstruction(code, prefix1, false, Operand.port(0), Operand.reg8(REG_A), true);
            } else {
                return null;
            }
        } else if ((prefix1 == 0xED) && (code >= 0x40) && (code <= 0x7F)){

            int c2 = code & 0x38;
            Operand op;
            if (c2 == 0x00) {
                op = Operand.reg8(REG_B);
            } else if (c2 == 0x08) {
                op = Operand.reg8(Operand.Reg8.REG_C);
            } else if (c2 == 0x10) {
                op = Operand.reg8(Operand.Reg8.REG_D);
            } else if (c2 == 0x18) {
                op = Operand.reg8(Operand.Reg8.REG_E);
            } else if (c2 == 0x20) {
                op = Operand.reg8(Operand.Reg8.REG_D);
            } else if (c2 == 0x28) {
                op = Operand.reg8(Operand.Reg8.REG_E);
            } else if (c2 == 0x30) {
                op = Operand.reg8(null);
            } else if (c2 == 0x38) {
                op = Operand.reg8(Operand.Reg8.REG_A);
            } else {
                throw new RuntimeException();
            }

            int c1 = code & 0x07;
            if (c1 == 0) {
                return new IoInstruction(code, prefix1, true, op, Operand.memRegPtr(Operand.Reg16.REG_BC), false);
            } else if (c1 == 1){
                return new IoInstruction(code, prefix1, false, Operand.memRegPtr(Operand.Reg16.REG_BC), op, false);
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return in ? "IN" : "OUT";
    }

    public String[] getAllMnemonics() {
        return new String[] {"IN", "OUT"};
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
