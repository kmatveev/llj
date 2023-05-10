package llj.asm.z80;

public class BitsInstruction extends Instruction {

    public static enum Type {CHECK, SET, RES}

    public final Type type;
    public final int bitIndex;
    public final Operand op;

    public BitsInstruction(Type type, int bitIndex, Operand op) throws IncorrectOperandException {
        this.type = type;
        this.bitIndex = bitIndex;
        this.op = op;
        getCode(type, bitIndex, op);
    }

    private BitsInstruction(int code, int prefix1, int prefix2, Type type, int bitIndex, Operand op, boolean partial) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.type = type;
        this.bitIndex = bitIndex;
        this.op = op;

    }

    public void getCode(Type type, int bitIndex, Operand op) throws IncorrectOperandException {
        int code, prefix1, prefix2;
        if (type == Type.CHECK) {
            code = 0x40;
        } else if (type == Type.SET) {
            code = 0xC0;
        } else if (type == Type.RES) {
            code = 0x80;
        } else {
            throw new RuntimeException();
        }

        if ((bitIndex >= 0 ) && (bitIndex <=7)) {
            code += (bitIndex * 8);
        }

        prefix1 = 0xCB;
        prefix2 = -1;
        if (op.type == Operand.Type.REG8) {
            if (op.reg8 == Operand.Reg8.REG_B) {
                code += 0;
            } else if (op.reg8 == Operand.Reg8.REG_C) {
                code += 1;
            } else if (op.reg8 == Operand.Reg8.REG_D) {
                code += 2;
            } else if (op.reg8 == Operand.Reg8.REG_E) {
                code += 3;
            } else if (op.reg8 == Operand.Reg8.REG_H) {
                code += 4;
            } else if (op.reg8 == Operand.Reg8.REG_L) {
                code += 5;
            } else if (op.reg8 == Operand.Reg8.REG_A) {
                code += 7;
            } else {
                throw new IncorrectOperandException();
            }
        } else if (op.type == Operand.Type.MEM_PTR_REG) {
            if (op.reg16 == Operand.Reg16.REG_HL) {
                code += 6;
            } else if (op.reg16 == Operand.Reg16.REG_IX) {
                prefix2 = prefix1;
                prefix1 = 0xDD;
                code += 6;
            } else if (op.reg16 == Operand.Reg16.REG_IY) {
                prefix2 = prefix1;
                prefix1 = 0xFD;
                code += 6;
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            throw new IncorrectOperandException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
    }

    public static BitsInstruction decodeInitial(int code, int prefix1, int prefix2) {
        int indexPrefix = -1;
        if ((prefix1 == 0xDD) || (prefix1 == 0xFD)) {
            if (prefix2 != 0xCB) {
                return null;
            }
            indexPrefix = prefix1;
        } else if (prefix1 != 0xCB) {
            return null;
        }

        boolean partial = false;

        Type type;
        if ((code >= 0x40) && (code <= 0x7F)) {
            type = Type.CHECK;
        } else if ((code >= 0x80) && (code <= 0xBF)) {
            type = Type.RES;
        } else if ((code >= 0xC0) && (code <= 0xFF)) {
            type = Type.SET;
        } else {
            return null;
        }

        int c1 = code & 0x07;
        Operand op;
        if (c1 == 0) {
            op = Operand.reg8(Operand.Reg8.REG_B);
        } else if (c1 == 1) {
            op = Operand.reg8(Operand.Reg8.REG_C);
        } else if (c1 == 2) {
            op = Operand.reg8(Operand.Reg8.REG_D);
        } else if (c1 == 3) {
            op = Operand.reg8(Operand.Reg8.REG_E);
        } else if (c1 == 4) {
            op = Operand.reg8(Operand.Reg8.REG_H);
        } else if (c1 == 5) {
            op = Operand.reg8(Operand.Reg8.REG_L);
        } else if (c1 == 6) {
            if (indexPrefix == IX_PREFIX) {
                partial = true;
                op = Operand.memRegPtr(Operand.Reg16.REG_IX);
            } else if (indexPrefix == IY_PREFIX) {
                partial = true;
                op = Operand.memRegPtr(Operand.Reg16.REG_IY);
            } else {
                op = Operand.memRegPtr(Operand.Reg16.REG_HL);
            }
        } else if (c1 == 7) {
            op = Operand.reg8(Operand.Reg8.REG_A);
        } else {
            throw new RuntimeException();
        }

        int c2 = code & 0x38;
        int bitIndex = c2 / 8;

        return new BitsInstruction(code, prefix1, prefix2, type, bitIndex, op, partial);

    }

    @Override
    public String getMnemonic() {
        return type == Type.CHECK ? "BIT" : type.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"BIT", "SET", "RES"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (op.type == Operand.Type.MEM_PTR_REG) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                op.indexOffset = data[offset];
                offset++;
            }
        }
        return offset;
    }

    @Override
    public int getSize() {
        int size = getOperationSize();
        if (op.type == Operand.Type.MEM_PTR_REG) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                size++;
            }
        }
        return size;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        if (op.type == Operand.Type.MEM_PTR_REG) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                dest[offset] = (byte)op.indexOffset;
                offset++;
            }
        }
        return offset;
    }
}
