package llj.asm.z80;

public class IncDecInstruction extends Instruction {

    public static enum Operation {
        INC, DEC
    }

    public final Operation operation;
    public final Operand op;

    public IncDecInstruction(Operation operation, Operand op) throws IncorrectOperandException {
        this.operation = operation;
        this.op = op;
        getCode(operation, op);
    }

    private IncDecInstruction(int code, int prefix1, Operation operation, Operand op, boolean partial) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.operation = operation;
        this.op = op;
    }

    public void getCode(Operation operation, Operand op) throws IncorrectOperandException {
        int code;
        int prefix1 = -1;
        if (op.type == Operand.Type.REG16) {

            if (operation == Operation.INC) {
                code = 0x03;
            } else if (operation == Operation.DEC) {
                code = 0x0B;
            } else {
                throw new RuntimeException();
            }

            if (op.reg16 == Operand.Reg16.REG_BC) {
                code += 0x00;
            } else if (op.reg16 == Operand.Reg16.REG_DE) {
                code += 0x10;
            } else if (op.reg16 == Operand.Reg16.REG_HL) {
                code += 0x20;
            } else if (op.reg16 == Operand.Reg16.REG_IX) {
                prefix1 = IX_PREFIX;
                code += 0x20;
            } else if (op.reg16 == Operand.Reg16.REG_IY) {
                prefix1 = IY_PREFIX;
                code += 0x20;
            } else if (op.reg16 == Operand.Reg16.REG_SP) {
                code += 0x30;
            } else {
                throw new RuntimeException();
            }
        } else if ((op.type == Operand.Type.REG8) || (op.type == Operand.Type.MEM_PTR_REG16)) {

            if (op.reg8 == Operand.Reg8.REG_B) {
                code = 0x04;
            } else if (op.reg8 == Operand.Reg8.REG_C) {
                code = 0x0C;
            } else if (op.reg8 == Operand.Reg8.REG_D) {
                code = 0x14;
            } else if (op.reg8 == Operand.Reg8.REG_E) {
                code = 0x1C;
            } else if (op.reg8 == Operand.Reg8.REG_H) {
                code = 0x24;
            } else if (op.reg8 == Operand.Reg8.REG_IXH) {
                prefix1 = IX_PREFIX;
                code = 0x24;
            } else if (op.reg8 == Operand.Reg8.REG_IYH) {
                prefix1 = IY_PREFIX;
                code = 0x24;
            } else if (op.reg8 == Operand.Reg8.REG_L) {
                code = 0x2C;
            } else if (op.reg8 == Operand.Reg8.REG_IXL) {
                prefix1 = IX_PREFIX;
                code = 0x2C;
            } else if (op.reg8 == Operand.Reg8.REG_IYL) {
                prefix1 = IY_PREFIX;
                code = 0x2C;
            } else if (op.reg16 == Operand.Reg16.REG_HL) {
                code = 0x34;
            } else if (op.reg16 == Operand.Reg16.REG_IX) {
                prefix1 = IX_PREFIX;
                code = 0x34;
            } else if (op.reg16 == Operand.Reg16.REG_IY) {
                prefix1 = IY_PREFIX;
                code = 0x34;
            } else if (op.reg8 == Operand.Reg8.REG_A) {
                code = 0x3C;
            } else {
                throw new IncorrectOperandException();
            }

            if (operation == Operation.INC) {
                code += 0x00;
            } else if (operation == Operation.DEC) {
                code += 0x01;
            } else {
                throw new RuntimeException();
            }

        } else {
            throw new IncorrectOperandException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static IncDecInstruction decodeInitial(int code, int prefix1) {
        code = code & 0xFF;
        boolean partial = false;

        if ((prefix1 == -1 ) || (prefix1 == IX_PREFIX)  || (prefix1 == IY_PREFIX)) {
            if ((code >= 0) && (code < 0x3F)) {
                int c1 = code & 0x07;
                if (c1 == 0x03) {

                    int checkedPrefix = -1;

                    Operation operation;
                    if ((code & 0x0F) == 0x0B) {
                        operation = Operation.DEC;
                    } else {
                        operation = Operation.INC;
                    }

                    Operand op;
                    int c3 = code & 0xF0;
                    if (c3 == 0x00) {
                        op = Operand.reg16(Operand.Reg16.REG_BC);
                    } else if (c3 == 0x10) {
                        op = Operand.reg16(Operand.Reg16.REG_DE);
                    } else if (c3 == 0x20) {
                        if (prefix1 == IX_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg16(Operand.Reg16.REG_IX);
                        } else if (prefix1 == IY_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg16(Operand.Reg16.REG_IY);
                        } else {
                            op = Operand.reg16(Operand.Reg16.REG_HL);
                        }
                    } else if (c3 == 0x30) {
                        op = Operand.reg16(Operand.Reg16.REG_SP);
                    } else {
                        throw new RuntimeException();
                    }

                    if (checkedPrefix != prefix1) {
                        return null;
                    }

                    return new IncDecInstruction(code, prefix1, operation, op, false);

                } else if ((c1 == 0x04) || (c1 == 0x05)) {

                    int checkedPrefix = -1;

                    Operation operation;
                    if (c1 == 0x04) {
                        operation = Operation.INC;
                    } else if (c1 == 0x05) {
                        operation = Operation.DEC;
                    } else {
                        throw new RuntimeException();
                    }

                    Operand op;
                    int c2 = code & 0x38;
                    if (c2 == 0x00) {
                        op = Operand.reg8(Operand.Reg8.REG_B);
                    } else if (c2 == 0x08) {
                        op = Operand.reg8(Operand.Reg8.REG_C);
                    } else if (c2 == 0x10) {
                        op = Operand.reg8(Operand.Reg8.REG_D);
                    } else if (c2 == 0x18) {
                        op = Operand.reg8(Operand.Reg8.REG_E);
                    } else if (c2 == 0x20) {
                        if (prefix1 == IX_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg8(Operand.Reg8.REG_IXH);
                        } else if (prefix1 == IY_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg8(Operand.Reg8.REG_IYH);
                        } else {
                            op = Operand.reg8(Operand.Reg8.REG_H);
                        }
                    } else if (c2 == 0x28) {
                        if (prefix1 == IX_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg8(Operand.Reg8.REG_IXL);
                        } else if (prefix1 == IY_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.reg8(Operand.Reg8.REG_IYL);
                        } else {
                            op = Operand.reg8(Operand.Reg8.REG_L);
                        }
                    } else if (c2 == 0x30) {
                        if (prefix1 == IX_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.memRegPtrIndex(Operand.Reg16.REG_IX, 0);
                            partial = true;
                        } else if (prefix1 == IY_PREFIX) {
                            checkedPrefix = prefix1;
                            op = Operand.memRegPtrIndex(Operand.Reg16.REG_IY, 0);
                            partial = true;
                        } else {
                            op = Operand.memRegPtr(Operand.Reg16.REG_HL);
                        }
                    } else if (c2 == 0x38) {
                        op = Operand.reg8(Operand.Reg8.REG_A);
                    } else {
                        throw new RuntimeException();
                    }

                    if (checkedPrefix != prefix1) {
                        return null;
                    }

                    return new IncDecInstruction(code, prefix1, operation, op, partial);

                } else {
                    return null;
                }

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return operation.name();
    }

    public String[] getAllMnemonics() {
        return new String[] {"INC", "DEC"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (op.type == Operand.Type.MEM_PTR_REG16) {
            if ((op.reg16 == Operand.Reg16.REG_IX) || (op.reg16 == Operand.Reg16.REG_IY)) {
                data[offset] = (byte)op.indexOffset;
                offset++;
            }
        }
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
