package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.REG_IX;
import static llj.asm.z80.Instruction.Operand.Reg16.REG_IY;

public class Op2Instruction extends Instruction {

    public static enum Operation {
        ADD, ADC, SUB, SBC, AND, OR, XOR, CP
    }

    public final Operation operation;
    public final Instruction.Operand opDest, opSrc;

    public Op2Instruction(Operation operation, Instruction.Operand opDest, Instruction.Operand opSrc) throws IncorrectOperandException {
        this.operation = operation;
        this.opDest = opDest;
        this.opSrc = opSrc;
        getCode(operation, opDest, opSrc);
    }

    private Op2Instruction(int code, int prefix1, Operation operation, Instruction.Operand opDest, Instruction.Operand opSrc, boolean partial) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.operation = operation;
        this.opDest = opDest;
        this.opSrc = opSrc;
    }

    public void getCode(Operation operation, Instruction.Operand opDest, Instruction.Operand opSrc) throws IncorrectOperandException {
        int code;
        int prefix1 = -1;
        if ((opDest.type == Operand.Type.REG8) && ((opSrc.type == Operand.Type.REG8) || (opSrc.type == Operand.Type.MEM_PTR_REG16))) {
            if ( opDest.reg8 != Operand.Reg8.REG_A) {
                throw new IncorrectOperandException();
            }

            if (operation == Operation.ADD) {
                code = 0x80;
            } else if (operation == Operation.ADC ){
                code = 0x88;
            } else if (operation == Operation.SUB ){
                code = 0x90;
            } else if (operation == Operation.SBC ){
                code = 0x98;
            } else if (operation == Operation.AND ){
                code = 0xA0;
            } else if (operation == Operation.XOR ){
                code = 0xA8;
            } else if (operation == Operation.OR ){
                code = 0xB0;
            } else if (operation == Operation.CP ){
                code = 0xB8;
            } else {
                throw new RuntimeException();
            }

            if (opSrc.reg8 == Operand.Reg8.REG_B) {
                code += 0x00;
            } else if (opSrc.reg8 == Operand.Reg8.REG_C) {
                code += 0x01;
            } else if (opSrc.reg8 == Operand.Reg8.REG_D) {
                code += 0x02;
            } else if (opSrc.reg8 == Operand.Reg8.REG_E) {
                code += 0x03;
            } else if (opSrc.reg8 == Operand.Reg8.REG_H) {
                code += 0x04;
            } else if (opSrc.reg8 == Operand.Reg8.REG_IXH) {
                prefix1 = IX_PREFIX;
                code += 0x04;
            } else if (opSrc.reg8 == Operand.Reg8.REG_IYH) {
                prefix1 = IY_PREFIX;
                code += 0x04;
            } else if (opSrc.reg8 == Operand.Reg8.REG_L) {
                code += 0x05;
            } else if (opSrc.reg8 == Operand.Reg8.REG_IXL) {
                prefix1 = IX_PREFIX;
                code += 0x05;
            } else if (opSrc.reg8 == Operand.Reg8.REG_IYL) {
                prefix1 = IY_PREFIX;
                code += 0x05;
            } else if (opSrc.reg16 == Operand.Reg16.REG_HL) {
                code += 0x06;
            } else if (opSrc.reg16 == Operand.Reg16.REG_IX) {
                prefix1 = IX_PREFIX;
                code += 0x06;
            } else if (opSrc.reg16 == Operand.Reg16.REG_IY) {
                prefix1 = IY_PREFIX;
                code += 0x06;
            } else if (opSrc.reg8 == Operand.Reg8.REG_A) {
                code += 0x07;
            } else {
                throw new IncorrectOperandException();
            }

        } else if ((opDest.type == Operand.Type.REG8) && (opSrc.type == Operand.Type.IMM8)) {

            if (opDest.reg8 != Operand.Reg8.REG_A) {
                throw new IncorrectOperandException();
            }

            if (operation == Operation.ADD) {
                code = 0xC6;
            } else if (operation == Operation.ADC) {
                code = 0xCE;
            } else if (operation == Operation.SUB) {
                code = 0xD6;
            } else if (operation == Operation.SBC) {
                code = 0xDE;
            } else if (operation == Operation.AND) {
                code = 0xE6;
            } else if (operation == Operation.XOR) {
                code = 0xEE;
            } else if (operation == Operation.OR) {
                code = 0xF6;
            } else if (operation == Operation.CP) {
                code = 0xFE;
            } else {
                throw new RuntimeException();
            }
        } else if ((opDest.type == Operand.Type.REG16) && (opSrc.type == Operand.Type.REG16)) {
            if (opDest.reg16 == Operand.Reg16.REG_HL) {
                prefix1 = -1;
            } else if (opDest.reg16 == Operand.Reg16.REG_IX) {
                prefix1 = IX_PREFIX;
            } else if (opDest.reg16 == Operand.Reg16.REG_IY) {
                prefix1 = IY_PREFIX;
            } else {
                throw new IncorrectOperandException();
            }

            if (opSrc.reg16 == Operand.Reg16.REG_BC) {
                code = 0x09;
            } else if (opSrc.reg16 == Operand.Reg16.REG_DE) {
                code = 0x19;
            } else if (opSrc.reg16 == Operand.Reg16.REG_HL) {
                if (opDest.reg16  == Operand.Reg16.REG_HL) {
                    code = 0x29;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (opSrc.reg16 == Operand.Reg16.REG_IX) {
                if (opDest.reg16  == Operand.Reg16.REG_IX) {
                    code = 0x29;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (opSrc.reg16 == Operand.Reg16.REG_IY) {
                if (opDest.reg16  == Operand.Reg16.REG_IY) {
                    code = 0x29;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (opSrc.reg16 == Operand.Reg16.REG_SP) {
                code = 0x39;
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            throw new IncorrectOperandException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static Op2Instruction decodeInitial(int code, int prefix1) {
        code = code & 0xFF;

        Operand opDest = Operand.reg8(Operand.Reg8.REG_A);

        if (((prefix1 == -1) || (prefix1 == IX_PREFIX) || (prefix1 == IY_PREFIX)) && (code >= 0x80) && (code <= 0xBF)) {
            int src = code & 0x07;
            int opc = code & 0xF8;

            int checkedPrefix = -1;

            Operation operation;
            Operand opSrc;
            if (opc == 0x80) {
                operation = Operation.ADD;
            } else if (opc == 0x88) {
                operation = Operation.ADC;
            } else if (opc == 0x90) {
                operation = Operation.SUB;
            } else if (opc == 0x98) {
                operation = Operation.SBC;
            } else if (opc == 0xA0) {
                operation = Operation.AND;;
            } else if (opc == 0xA8) {
                operation = Operation.XOR;
            } else if (opc == 0xB0) {
                operation = Operation.OR;
            } else if (opc == 0xB8) {
                operation = Operation.CP;
            } else {
                throw new RuntimeException();
            }

            if (src == 0) {
                opSrc = Operand.reg8(Operand.Reg8.REG_B);
            } else if (src == 0x01) {
                opSrc = Operand.reg8(Operand.Reg8.REG_C);
            } else if (src == 0x02) {
                opSrc = Operand.reg8(Operand.Reg8.REG_D);
            } else if (src == 0x03) {
                opSrc = Operand.reg8(Operand.Reg8.REG_E);
            } else if (src == 0x04) {
                if (prefix1 == IX_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.reg8(Operand.Reg8.REG_IXH);
                } else if (prefix1 == IY_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.reg8(Operand.Reg8.REG_IYH);
                } else {
                    opSrc = Operand.reg8(Operand.Reg8.REG_H);
                }
            } else if (src == 0x05) {
                if (prefix1 == IX_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.reg8(Operand.Reg8.REG_IXL);
                } else if (prefix1 == IY_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.reg8(Operand.Reg8.REG_IYL);
                } else {
                    opSrc = Operand.reg8(Operand.Reg8.REG_L);
                }
            } else if (src == 0x06) {
                if (prefix1 == IX_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.memRegPtrIndex(Operand.Reg16.REG_IX, 0);
                } else if (prefix1 == IY_PREFIX) {
                    checkedPrefix = prefix1;
                    opSrc = Operand.memRegPtrIndex(Operand.Reg16.REG_IY, 0);
                } else {
                    opSrc = Operand.memRegPtr(Operand.Reg16.REG_HL);
                }
            } else if (src == 0x07) {
                opSrc = Operand.reg8(Operand.Reg8.REG_A);
            } else {
                throw new RuntimeException();
            }

            if (checkedPrefix != prefix1) {
                return null;
            }

            return new Op2Instruction(code, prefix1, operation, opDest, opSrc, false);
        } else if ((prefix1 == -1) && (code >= 0xC0) && (code <= 0xFF) && ((code & 0x07) == 0x06)) {

            Operation operation;
            if (code == 0xC6) {
                operation = Operation.ADD;
            } else if (code == 0xCE) {
                operation = Operation.ADC;
            } else if (code == 0xD6) {
                operation = Operation.SUB;
            } else if (code == 0xDE) {
                operation = Operation.SBC;
            } else if (code == 0xE6) {
                operation = Operation.AND;
            } else if (code == 0xEE) {
                operation = Operation.XOR;
            } else if (code == 0xF6) {
                operation = Operation.OR;
            } else if (code == 0xFE) {
                operation = Operation.CP;
            } else {
                throw new RuntimeException();
            }

            return new Op2Instruction(code, prefix1, operation, opDest, Operand.imm8(0), true);
        } else if (((prefix1 == -1) || (prefix1 == IX_PREFIX) || (prefix1 == IY_PREFIX)) && ((code == 0x09) || (code == 0x19) || (code == 0x29) || code == 0x39)) {
            if (prefix1 == IX_PREFIX) {
                opDest = Operand.reg16(Operand.Reg16.REG_IX);
            } else if (prefix1 == IY_PREFIX) {
                opDest = Operand.reg16(Operand.Reg16.REG_IY);
            } else {
                opDest = Operand.reg16(Operand.Reg16.REG_HL);
            }
            Operand opSrc;
            if (code == 0x09) {
                opSrc = Operand.reg16(Operand.Reg16.REG_BC);
            } else if (code == 0x19) {
                opSrc = Operand.reg16(Operand.Reg16.REG_DE);
            } else if (code == 0x29) {
                opSrc = opDest;
            } else if (code == 0x39) {
                opSrc = Operand.reg16(Operand.Reg16.REG_SP);
            } else {
                throw new RuntimeException();
            }
            return new Op2Instruction(code, prefix1, Operation.ADD, opDest, opSrc, false);
        } else if ((prefix1 == 0xED) && ((code == 0x42) || (code == 0x52) || (code == 0x62) || (code == 0x72))) {
            opDest = Operand.reg16(Operand.Reg16.REG_HL);
            Operand opSrc;
            if (code == 0x42) {
                opSrc = Operand.reg16(Operand.Reg16.REG_BC);
            } else if (code == 0x52) {
                opSrc = Operand.reg16(Operand.Reg16.REG_DE);
            } else if (code == 0x62) {
                opSrc = opDest;
            } else if (code == 0x72) {
                opSrc = Operand.reg16(Operand.Reg16.REG_SP);
            } else {
                throw new RuntimeException();
            }
            return new Op2Instruction(code, prefix1, Operation.SBC, opDest, opSrc, false);
        } else if ((prefix1 == 0xED) && ((code == 0x4A) || (code == 0x5A) || (code == 0x6A) || (code == 0x7A))) {
            opDest = Operand.reg16(Operand.Reg16.REG_HL);
            Operand opSrc;
            if (code == 0x4A) {
                opSrc = Operand.reg16(Operand.Reg16.REG_BC);
            } else if (code == 0x5A) {
                opSrc = Operand.reg16(Operand.Reg16.REG_DE);
            } else if (code == 0x6A) {
                opSrc = opDest;
            } else if (code == 0x7A) {
                opSrc = Operand.reg16(Operand.Reg16.REG_SP);
            } else {
                throw new RuntimeException();
            }
            return new Op2Instruction(code, prefix1, Operation.ADC, opDest, opSrc, false);

        } else {
            return null;
        }
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (opSrc.type == Operand.Type.IMM8) {
            opSrc.imm = data[offset] & 0xFF;
            offset++;
        } else if (opSrc.type == Operand.Type.MEM_PTR_REG16) {
            if ((opSrc.reg16 == REG_IX) || (opSrc.reg16 == REG_IY)) {
                opSrc.indexOffset = data[offset];
                offset++;
            }
        }
        return offset;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        if (opSrc.type == Operand.Type.IMM8) {
            dest[offset] = (byte)(opSrc.imm & 0xFF);
            offset++;
        } else if (opSrc.type == Operand.Type.MEM_PTR_REG16) {
            if ((opSrc.reg16 == REG_IX) || (opSrc.reg16 == REG_IY)) {
                dest[offset] = (byte) opSrc.indexOffset;
                offset++;
            }
        }
        return offset;

    }

    @Override
    public String getMnemonic() {
        return operation.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"ADD", "ADC", "SUB", "SBC", "AND", "XOR", "OR", "CP"};
    }

    @Override
    public int getSize() {
        int size = getOperationSize();
        if (opSrc.type == Operand.Type.IMM8) {
            return size + 1;
        } else {
            return size;
        }
    }
}
