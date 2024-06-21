package llj.asm.z80;

public class ShiftRotateInstruction extends Instruction {

    public static enum Type {
        RLC(true, true, false, false, false), RLCA(true, true, false, false, true), RL(true, true, true, false, false), RLA(true, true, true, false, true),
        RRC(true, false, false, false, false), RRCA(true, false, false, false, true), RR(true, false, true, false, false), RRA(true, false, true, false, true),
        SLA(false, true, true, true, false), SRA(false, false, true, true, false),
        SLL(false, true, true, false, false), SRL(false, false, true, false, false);

        Type(boolean rotate, boolean left, boolean throughCarry, boolean arithmetical, boolean shortForm) {
            this.rotate = rotate;
            this.left = left;
            this.throughCarry = throughCarry;
            this.arithmetical = arithmetical;
            this.shortForm = shortForm;
        }

        public final boolean rotate;
        public final boolean left;
        public final boolean throughCarry;
        public final boolean arithmetical;
        public final boolean shortForm;

    }

    public final Type type;
    public final Operand op;

    public ShiftRotateInstruction(Type type, Operand op) throws IncorrectOperandException {
        this.type = type;
        this.op = op;
        getCode(type, op);
    }

    private ShiftRotateInstruction(int code, int prefix1, int prefix2, Type type, Operand op, boolean partial) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.type = type;
        this.op = op;
    }

    public void getCode(Type type, Operand op) throws IncorrectOperandException {
        int code, prefix1, prefix2 = -1;
        if (type == Type.RRCA) {
            if ((op.type == Operand.Type.REG8) && (op.reg8 == Operand.Reg8.REG_A)) {
                code = 0x0F;
                prefix1 = -1;
                prefix2 = -1;
            } else {
                throw new IncorrectOperandException();
            }
        } else if (type == Type.RLCA) {
            if ((op.type == Operand.Type.REG8) && (op.reg8 == Operand.Reg8.REG_A)) {
                code = 0x07;
                prefix1 = -1;
                prefix2 = -1;
            } else {
                throw new IncorrectOperandException();
            }

        } else if (type == Type.RRA) {
            if ((op.type == Operand.Type.REG8) && (op.reg8 == Operand.Reg8.REG_A)) {
                code = 0x1F;
                prefix1 = -1;
                prefix2 = -1;
            } else {
                throw new IncorrectOperandException();
            }
        } else if (type == Type.RLA) {
            if ((op.type == Operand.Type.REG8) && (op.reg8 == Operand.Reg8.REG_A)) {
                code = 0x17;
                prefix1 = -1;
                prefix2 = -1;
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            prefix1 = 0xCB;
            if (type == Type.RLC) {
                code = 0x00;
            } else if (type == Type.RRC) {
                code = 0x08;
            } else if (type == Type.RL) {
                code = 0x10;
            } else if (type == Type.RR) {
                code = 0x18;
            } else if (type == Type.SLA) {
                code = 0x20;
            } else if (type == Type.SRA) {
                code = 0x28;
            } else if (type == Type.SLL) {
                code = 0x30;
            } else if (type == Type.SRL) {
                code = 0x38;
            } else {
                throw new RuntimeException();
            }

            if (op.type == Operand.Type.REG8) {
                if (op.reg8 == Operand.Reg8.REG_B) {
                    code += 0x00;
                } else if (op.reg8 == Operand.Reg8.REG_C) {
                    code += 0x01;
                } else if (op.reg8 == Operand.Reg8.REG_D) {
                    code += 0x02;
                } else if (op.reg8 == Operand.Reg8.REG_E) {
                    code += 0x03;
                } else if (op.reg8 == Operand.Reg8.REG_H) {
                    code += 0x04;
                } else if (op.reg8 == Operand.Reg8.REG_L) {
                    code += 0x05;
                } else if (op.reg8 == Operand.Reg8.REG_A) {
                    code += 0x07;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (op.type == Operand.Type.MEM_PTR_REG16) {
                if (op.reg16 == Operand.Reg16.REG_IX) {
                    prefix2 = prefix1;
                    prefix1 = IX_PREFIX;
                } else if (op.reg16 == Operand.Reg16.REG_IY) {
                    prefix2 = prefix1;
                    prefix1 = IY_PREFIX;
                } else if (op.reg16 == Operand.Reg16.REG_HL) {
                    // nothing
                } else {
                    throw new IncorrectOperandException();
                }
                code += 0x06;
            } else {
                throw new IncorrectOperandException();
            }
        }
        this.opCode = code;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
    }

    public static ShiftRotateInstruction decodeInitial(int code, int prefix1, int prefix2) {
        if (prefix1 == -1) {
            Type type;
            Operand op = Operand.reg8(Operand.Reg8.REG_A);
            if (code == 0x07) {
                type = Type.RLCA;
            } else if (code == 0x0F) {
                type = Type.RRCA;
            } else if (code == 0x17) {
                type = Type.RLA;
            } else if (code == 0x1F) {
                type = Type.RRA;
            } else {
                return null;
            }
            return new ShiftRotateInstruction(code, prefix1, prefix2, type, op, false);
        } else {

            int indexPrefix = -1;
            if ((prefix1 == IX_PREFIX) || (prefix1 == IY_PREFIX)) {
                if (prefix2 != 0xCB) {
                    return null;
                }
                indexPrefix = prefix1;
            } else if (prefix1 != 0xCB) {
                return null;
            }

            boolean partial = false;

            Type type;
            if ((code >= 0x00) && (code <= 0x07)) {
                type = Type.RLC;
            } else if ((code >= 0x08) && (code <= 0x0F)) {
                type = Type.RRC;
            } else if ((code >= 0x10) && (code <= 0x17)) {
                type = Type.RL;
            } else if ((code >= 0x18) && (code <= 0x1F)) {
                type = Type.RR;
            } else if ((code >= 0x20) && (code <= 0x27)) {
                type = Type.SLA;
            } else if ((code >= 0x28) && (code <= 0x2F)) {
                type = Type.SRA;
            } else if ((code >= 0x30) && (code <= 0x37)) {
                type = Type.SLL;
            } else if ((code >= 0x38) && (code <= 0x3F)) {
                type = Type.SRL;
            } else {
                return null;
            }

            Operand op;
            // although this behaviour is undocumented, if ix/iy prefix is present then op is memptr_reg, regardless of c1
            if (indexPrefix == IX_PREFIX) {
                op = Operand.memRegPtrIndex(Operand.Reg16.REG_IX, 0);
                partial = true;
            } else if (indexPrefix == IY_PREFIX) {
                partial = true;
                op = Operand.memRegPtrIndex(Operand.Reg16.REG_IY, 0);
            } else {
                int cr = code & 0x07;
                if (cr == 0x00) {
                    op = Operand.reg8(Operand.Reg8.REG_B);
                } else if (cr == 0x01) {
                    op = Operand.reg8(Operand.Reg8.REG_C);
                } else if (cr == 0x02) {
                    op = Operand.reg8(Operand.Reg8.REG_D);
                } else if (cr == 0x03) {
                    op = Operand.reg8(Operand.Reg8.REG_E);
                } else if (cr == 0x04) {
                    op = Operand.reg8(Operand.Reg8.REG_H);
                } else if (cr == 0x05) {
                    op = Operand.reg8(Operand.Reg8.REG_L);
                } else if (cr == 0x06) {
                    op = Operand.memRegPtr(Operand.Reg16.REG_HL);
                } else if (cr == 0x07) {
                    op = Operand.reg8(Operand.Reg8.REG_A);
                } else {
                    throw new RuntimeException();
                }
            }
            return new ShiftRotateInstruction(code, prefix1, prefix2, type, op, partial);
        }
    }

    @Override
    public String getMnemonic() {
        return type.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"RLC", "RLCA", "RRC", "RRCA", "RL", "RLA", "RR", "RRA", "SLA", "SRA", "SLL", "SRL"};
    }

    public int decodeRemaining(byte[] data, int offset) {
        if (op.type == Operand.Type.MEM_PTR_REG16) {
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
        if (op.type == Operand.Type.MEM_PTR_REG16) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                size++;
            }
        }
        return size;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        if (op.type == Operand.Type.MEM_PTR_REG16) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                dest[offset] = (byte)op.indexOffset;
                offset++;
            }
        }
        return offset;
    }
}