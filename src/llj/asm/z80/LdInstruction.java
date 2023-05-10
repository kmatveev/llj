package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.*;

public class LdInstruction extends Instruction {

    public final Operand op1, op2;
    public int tstates;

    public LdInstruction(Operand op1, Operand op2) throws IncorrectOperandException {
        this.op1 = op1;
        this.op2 =op2;
        setOpCode(op1, op2);
    }

    public LdInstruction(int opCode, int prefix1, Operand op1, Operand op2, boolean partial, int tstates) {
        this.op1 = op1;
        this.op2 =op2;
        this.opCode = opCode;
        this.prefix1 = prefix1;
        this.tstates = tstates;
    }

    private void setOpCode(Operand op1, Operand op2) throws IncorrectOperandException {
        int code, prefix1 = -1;
        int tstates;
        if (op1.type == Operand.Type.REG16) {
            if (op2.type == Operand.Type.IMM16) {
                tstates = 4 + 6;  // opcode fetch, imm16 fetch
                if (op1.reg16 == REG_BC) {
                    code = 0x01;
                } else if (op1.reg16 == Operand.Reg16.REG_DE) {
                    code = 0x11;
                } else if (op1.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x21;
                } else if (op1.reg16 == REG_IX) {
                    tstates += 4;  // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x21;
                } else if (op1.reg16 == REG_IY) {
                    tstates += 4;  // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x21;
                } else if (op1.reg16 == Operand.Reg16.REG_SP) {
                    code = 0x31;
                } else {
                    throw new RuntimeException();
                }
            } else if (op2.type == Operand.Type.REG16) {
                tstates = 4 + 2;  // opcode fetch, reg16 transfer
                if ((op1.reg16 == Operand.Reg16.REG_SP) && (op2.reg16 == Operand.Reg16.REG_HL)) {
                    code = 0xF9;
                } else if ((op1.reg16 == Operand.Reg16.REG_SP) && (op2.reg16 == REG_IX)) {
                    tstates += 4; // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0xF9;
                } else if ((op1.reg16 == Operand.Reg16.REG_SP) && (op2.reg16 == REG_IY)) {
                    tstates += 4; // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0xF9;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (op2.type == Operand.Type.MEM_PTR_IMM16) {
                tstates = 4 + 6 + 6; // opcode fetch, imm16 fetch, reg16 write
                if (op1.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x2A; // only HL pair has two choices: either short opcode 0x2A or longer 0xED 0x6B
                } else if (op1.reg16 == REG_IX) {
                    tstates += 4;  // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x2A;
                } else if (op1.reg16 == REG_IY) {
                    tstates += 4;  // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x2A;
                } else if (op1.reg16 == REG_BC) {
                    tstates += 4;  // prefix fetch
                    prefix1 = 0xED;
                    code = 0x4B;
                } else if (op1.reg16 == Operand.Reg16.REG_DE) {
                    tstates += 4;  // prefix fetch
                    prefix1 = 0xED;
                    code = 0x5B;
                } else if (op1.reg16 == Operand.Reg16.REG_SP) {
                    tstates += 4;  // prefix fetch
                    prefix1 = 0xED;
                    code = 0x7B;
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new RuntimeException();
            }
        } else if ((op1.type == Operand.Type.REG8) || (op1.type == Operand.Type.MEM_PTR_REG)) {

            if ((op2.type == Operand.Type.REG8) || (op2.type == Operand.Type.MEM_PTR_REG)) {

                tstates = ((op1.type == Operand.Type.REG8) && (op2.type == Operand.Type.REG8)) ? 4 : 7;

                if (op1.reg8 == Operand.Reg8.REG_B) {
                    code = 0x40;
                } else if (op1.reg8 == Operand.Reg8.REG_C) {
                    code = 0x48;
                } else if (op1.reg8 == Operand.Reg8.REG_D) {
                    code = 0x50;
                } else if (op1.reg8 == Operand.Reg8.REG_E) {
                    code = 0x58;
                } else if (op1.reg8 == Operand.Reg8.REG_H) {
                    code = 0x60;
                } else if (op1.reg8 == Operand.Reg8.REG_IXH) {
                    tstates += 4; // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x60;
                } else if (op1.reg8 == Operand.Reg8.REG_IYH) {
                    tstates += 4; // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x60;
                } else if (op1.reg8 == Operand.Reg8.REG_L) {
                    code = 0x68;
                } else if (op1.reg8 == Operand.Reg8.REG_IXL) {
                    tstates += 4; // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x68;
                } else if (op1.reg8 == Operand.Reg8.REG_IYL) {
                    tstates += 4; // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x68;
                } else if (op1.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x70;
                } else if (op1.reg16 == REG_IX) {
                    tstates = tstates + 4 + 5 + 3;  // prefix fetch, index fetch and indexing, mem write
                    prefix1 = IX_PREFIX;
                    code = 0x70;
                } else if (op1.reg16 == REG_IY) {
                    tstates = tstates + 4 + 5 + 3;  // prefix fetch, index fetch and indexing, mem write
                    prefix1 = IY_PREFIX;
                    code = 0x70;
                } else if (op1.reg8 == Operand.Reg8.REG_A) {
                    code = 0x78;
                } else if (op1.reg16 == REG_BC) {
                    code = 0x02;
                } else if (op1.reg16 == Operand.Reg16.REG_DE) {
                    code = 0x12;
                } else {
                    throw new RuntimeException();
                }


                // Special handling for LD (BC),A and LD (DE),A
                if ((op1.type == Operand.Type.MEM_PTR_REG) && (op1.reg16 == REG_BC)) {
                    if (op2.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    // don't need to update a code, it's already correct

                } else if ((op1.type == Operand.Type.MEM_PTR_REG) && (op1.reg16 == Operand.Reg16.REG_DE)) {
                    if (op2.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    // don't need to update a code, it's already correct

                } else if ((op1.type == Operand.Type.REG8) && (op1.reg8 == Operand.Reg8.REG_R)) {
                    if (op2.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    tstates += 5;
                    prefix1 = 0xED;
                    code = 0x4F;
                } else if ((op1.type == Operand.Type.REG8) && (op1.reg8 == Operand.Reg8.REG_I)) {
                    if (op2.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    tstates += 5;
                    prefix1 = 0xED;
                    code = 0x47;
                } else if ((op2.type == Operand.Type.REG8) && (op2.reg8 == Operand.Reg8.REG_R)) {
                    if (op1.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    tstates += 5;
                    prefix1 = 0xED;
                    code = 0x5F;
                } else if ((op2.type == Operand.Type.REG8) && (op2.reg8 == Operand.Reg8.REG_I)) {
                    if (op1.reg8 != Operand.Reg8.REG_A) {
                        throw new IncorrectOperandException();
                    }
                    tstates += 5;
                    prefix1 = 0xED;
                    code = 0x57;

                } else {

                    if (op2.reg8 == Operand.Reg8.REG_B) {
                        code += 0x00;
                    } else if (op2.reg8 == Operand.Reg8.REG_C) {
                        code += 0x01;
                    } else if (op2.reg8 == Operand.Reg8.REG_D) {
                        code += 0x02;
                    } else if (op2.reg8 == Operand.Reg8.REG_E) {
                        code += 0x03;
                    } else if (op2.reg8 == Operand.Reg8.REG_H) {
                        if ((op1.reg8 == Operand.Reg8.REG_IXH) || (op1.reg8 == Operand.Reg8.REG_IXL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x04;
                    } else if (op2.reg8 == Operand.Reg8.REG_IXH) {
                        if ((op1.reg8 == Operand.Reg8.REG_H) || (op1.reg8 == Operand.Reg8.REG_L) || (op1.reg16 == Operand.Reg16.REG_HL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 4; // prefix fetch
                        prefix1 = IX_PREFIX;
                        code += 0x04;
                    } else if (op2.reg8 == Operand.Reg8.REG_IYH) {
                        if ((op1.reg8 == Operand.Reg8.REG_H) || (op1.reg8 == Operand.Reg8.REG_L) || (op1.reg16 == Operand.Reg16.REG_HL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 4; // prefix fetch
                        prefix1 = IY_PREFIX;
                        code += 0x04;
                    } else if (op2.reg8 == Operand.Reg8.REG_L) {
                        if ((op1.reg8 == Operand.Reg8.REG_IXH) || (op1.reg8 == Operand.Reg8.REG_IXL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x05;
                    } else if (op2.reg8 == Operand.Reg8.REG_IXL) {
                        if ((op1.reg8 == Operand.Reg8.REG_H) || (op1.reg8 == Operand.Reg8.REG_L) || (op1.reg16 == Operand.Reg16.REG_HL)  || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 4; // prefix fetch
                        prefix1 = IX_PREFIX;
                        code += 0x05;
                    } else if (op2.reg8 == Operand.Reg8.REG_IYL) {
                        if ((op1.reg8 == Operand.Reg8.REG_H) || (op1.reg8 == Operand.Reg8.REG_L) || (op1.reg16 == Operand.Reg16.REG_HL)  || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 4; // prefix fetch
                        prefix1 = IY_PREFIX;
                        code += 0x05;
                    } else if (op2.reg16 == Operand.Reg16.REG_HL) {
                        if (op1.type == Operand.Type.MEM_PTR_REG) {
                            throw new IncorrectOperandException();
                        } else if ((op1.reg8 == Operand.Reg8.REG_IXH) || (op1.reg8 == Operand.Reg8.REG_IXL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x06;
                    } else if (op2.reg16 == REG_IX) {
                        if (op1.type == Operand.Type.MEM_PTR_REG) {
                            throw new IncorrectOperandException();
                        } else if ((op1.reg8 == Operand.Reg8.REG_IXH) || (op1.reg8 == Operand.Reg8.REG_IXL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 12;
                        prefix1 = IX_PREFIX;
                        code += 0x06;
                    } else if (op2.reg16 == REG_IY) {
                        if (op1.type == Operand.Type.MEM_PTR_REG) {
                            throw new IncorrectOperandException();
                        } else if ((op1.reg8 == Operand.Reg8.REG_IXH) || (op1.reg8 == Operand.Reg8.REG_IXL) || (op1.reg8 == Operand.Reg8.REG_IYH) || (op1.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        tstates += 12;
                        prefix1 = IY_PREFIX;
                        code += 0x06;
                    } else if (op2.reg8 == Operand.Reg8.REG_A) {
                        code += 0x07;
                    } else if (op2.reg16 == REG_BC) {
                        if ((op1.type == Operand.Type.REG8) && (op1.reg8 == Operand.Reg8.REG_A)) {
                            code = 0x0A;
                        } else {
                            throw new IncorrectOperandException();
                        }
                    } else if (op2.reg16 == Operand.Reg16.REG_DE) {
                        if ((op1.type == Operand.Type.REG8) && (op1.reg8 == Operand.Reg8.REG_A)) {
                            code = 0x1A;
                        } else {
                            throw new IncorrectOperandException();
                        }
                    } else {
                        throw new IncorrectOperandException();
                    }
                }
            } else if (op2.type == Operand.Type.IMM8) {
                tstates = 7;
                if (op1.reg8 == Operand.Reg8.REG_B) {
                    code = 0x06;
                } else if (op1.reg8 == Operand.Reg8.REG_C) {
                    code = 0x0E;
                } else if (op1.reg8 == Operand.Reg8.REG_D) {
                    code = 0x16;
                } else if (op1.reg8 == Operand.Reg8.REG_E) {
                    code = 0x1E;
                } else if (op1.reg8 == Operand.Reg8.REG_H) {
                    code = 0x26;
                } else if (op1.reg8 == Operand.Reg8.REG_IXH) {
                    tstates += 4; // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x26;
                } else if (op1.reg8 == Operand.Reg8.REG_IYH) {
                    tstates += 4; // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x26;
                } else if (op1.reg8 == Operand.Reg8.REG_L) {
                    code = 0x2E;
                } else if (op1.reg8 == Operand.Reg8.REG_IXL) {
                    tstates += 4; // prefix fetch
                    prefix1 = IX_PREFIX;
                    code = 0x2E;
                } else if (op1.reg8 == Operand.Reg8.REG_IYL) {
                    tstates += 4; // prefix fetch
                    prefix1 = IY_PREFIX;
                    code = 0x2E;
                } else if (op1.reg16 == Operand.Reg16.REG_HL) {
                    tstates += 3;
                    code = 0x36;
                } else if (op1.reg16 == REG_IX) {
                    tstates = tstates + 3 + 4 + 5; // imm8 fetch, prefix fetch, index fetch and indexing
                    prefix1 = IX_PREFIX;
                    code = 0x36;
                } else if (op1.reg16 == REG_IY) {
                    tstates = tstates + 3 + 4 + 5; // imm8 fetch, prefix fetch, index fetch and indexing
                    prefix1 = IY_PREFIX;
                    code = 0x36;
                } else if (op1.reg8 == Operand.Reg8.REG_A) {
                    code = 0x3E;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (op2.type == Operand.Type.MEM_PTR_IMM16) {
                if ((op1.type == Operand.Type.REG8) && (op1.reg8 == Operand.Reg8.REG_A)) {
                    tstates = 4 + 6 + 3; // opcode fetch, imm16 fetch, reg8 read
                    code = 0x3A;
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new RuntimeException();
            }
        } else if (op1.type == Operand.Type.MEM_PTR_IMM16) {
            if ((op2.type == Operand.Type.REG8) && (op2.reg8 == Operand.Reg8.REG_A)) {
                tstates = 4 + 6 + 3; // opcode fetch, imm16 fetch, reg8 write
                code = 0x32;
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == Operand.Reg16.REG_HL)) {
                tstates = 4 + 6 + 6; // opcode fetch, imm16 fetch, reg16 write
                code = 0x22; // only HL pair has two choices: either short opcode 0x22 or longer 0xED 0x63
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == REG_IX)) {
                tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 write
                prefix1 = IX_PREFIX;
                code = 0x22;
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == REG_IY)) {
                tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 write
                prefix1 = IY_PREFIX;
                code = 0x22;
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == REG_BC)) {
                tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 write
                prefix1 = 0xED;
                code = 0x43;
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == Operand.Reg16.REG_DE)) {
                tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 write
                prefix1 = 0xED;
                code = 0x53;
            } else if ((op2.type == Operand.Type.REG16) && (op2.reg16 == Operand.Reg16.REG_SP)) {
                tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 write
                prefix1 = 0xED;
                code = 0x73;
            } else {
                throw new IncorrectOperandException();
            }

        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
        this.tstates = tstates;
    }

    public static LdInstruction decodeInitial(int code, int prefix1) {
        prefix1 = prefix1;
        code = code & 0xFF;
        int tstates;

        if ((prefix1 == -1) || (prefix1 == IX_PREFIX) || (prefix1 == IY_PREFIX)) {
            if ((code >= 0x40) && (code <= 0x7F)) {
                if (code == 0x76) {
                    return null; // HALT code
                }

                // I'm too lazy to check prefix to be empty every time, so this will be default value to be checked at the end
                int checkedPrefix = -1;

                int src = code & 0x07;
                int dst = code & 0x78;

                Operand op1, op2;
                tstates = 4; // base value for reg-reg transfer
                if (dst == 0x40) {
                    op1 = Operand.reg8(Operand.Reg8.REG_B);
                } else if (dst == 0x48) {
                    op1 = Operand.reg8(Operand.Reg8.REG_C);
                } else if (dst == 0x50) {
                    op1 = Operand.reg8(Operand.Reg8.REG_D);
                } else if (dst == 0x58) {
                    op1 = Operand.reg8(Operand.Reg8.REG_E);
                } else if (dst == 0x60) {
                    if ((prefix1 == IX_PREFIX) && (src != 0x06)){
                        checkedPrefix = prefix1;
                        // in this case prefix is related to op2
                        op1 = Operand.reg8(Operand.Reg8.REG_IXH);
                        tstates += 4; // prefix fetching
                    } else if ((prefix1 == IY_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        // in this case prefix is related to op2
                        op1 = Operand.reg8(Operand.Reg8.REG_IYH);
                        tstates += 4; // prefix fetching
                    } else {
                        op1 = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (dst == 0x68) {
                    if ((prefix1 == IX_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        // in this case prefix is related to op2
                        op1 = Operand.reg8(Operand.Reg8.REG_IXL);
                        tstates += 4; // prefix fetching
                    } else if ((prefix1 == IY_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg8(Operand.Reg8.REG_IYL);
                        tstates += 4; // prefix fetching
                    } else {
                        op1 = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (dst == 0x70) {
                    tstates = 7;
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.memRegPtrIndex(REG_IX, 0);
                        tstates = tstates + 4 + 4 + 4; // prefix fetching, index fetching, indexing
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.memRegPtrIndex(REG_IY, 0);
                        tstates = tstates + 4 + 4 + 4; // prefix fetching, index fetching, indexing
                    } else {
                        op1 = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (dst == 0x78) {
                    op1 = Operand.reg8(Operand.Reg8.REG_A);
                } else {
                    throw new RuntimeException();
                }

                if (src == 0) {
                    op2 = Operand.reg8(Operand.Reg8.REG_B);
                } else if (src == 0x01) {
                    op2 = Operand.reg8(Operand.Reg8.REG_C);
                } else if (src == 0x02) {
                    op2 = Operand.reg8(Operand.Reg8.REG_D);
                } else if (src == 0x03) {
                    op2 = Operand.reg8(Operand.Reg8.REG_E);
                } else if (src == 0x04) {
                    if ((prefix1 == IX_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        op2 =  Operand.reg8(Operand.Reg8.REG_IXH);
                        tstates += 4; // prefix fetching
                    } else if ((prefix1 == IY_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        op2 = Operand.reg8(Operand.Reg8.REG_IYH);
                        tstates += 4; // prefix fetching
                    } else {
                        op2 = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (src == 0x05) {
                    if ((prefix1 == IX_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        op2 = Operand.reg8(Operand.Reg8.REG_IXL);
                        tstates += 4; // prefix fetching
                    } else if ((prefix1 == IY_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        op2 = Operand.reg8(Operand.Reg8.REG_IYL);
                        tstates += 4; // prefix fetching
                    } else {
                        op2 = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (src == 0x06) {
                    tstates = 7;
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op2 = Operand.memRegPtrIndex(REG_IX, 0);
                        tstates = tstates + 4 + 4 + 4; // prefix fetching, index fetching, indexing
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op2 = Operand.memRegPtrIndex(REG_IY, 0);
                        tstates = tstates + 4 + 4 + 4; // prefix fetching, index fetching, indexing
                    } else {
                        op2 = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (src == 0x07) {
                    op2 = Operand.reg8(Operand.Reg8.REG_A);
                } else {
                    throw new RuntimeException();
                }

                if (checkedPrefix != prefix1) {
                    return null;
                }

                return new LdInstruction(code, prefix1, op1, op2, false, tstates);
            } else if ((code >= 0) && (code <= 0x3F) && ((code & 0x07) == 0x06)) {
                int checkedPrefix = -1;
                Operand op1;
                tstates = 7;
                if (code == 0x06) {
                    op1 = Operand.reg8(Operand.Reg8.REG_B);
                } else if (code == 0x0E) {
                    op1 = Operand.reg8(Operand.Reg8.REG_C);
                } else if (code == 0x16) {
                    op1 = Operand.reg8(Operand.Reg8.REG_D);
                } else if (code == 0x1E) {
                    op1 = Operand.reg8(Operand.Reg8.REG_E);
                } else if (code == 0x26) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg8(Operand.Reg8.REG_IXH);
                        tstates += 4; // prefix fetching
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg8(Operand.Reg8.REG_IYH);
                        tstates += 4; // prefix fetching
                    } else {
                        op1 = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (code == 0x2E) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg8(Operand.Reg8.REG_IXL);
                        tstates += 4; // prefix fetching
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg8(Operand.Reg8.REG_IYL);
                        tstates += 4; // prefix fetching
                    } else {
                        op1 = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (code == 0x36) {
                    tstates = 10;
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.memRegPtrIndex(REG_IX, 0);
                        tstates = tstates + 4 + 5; // prefix fetching, index fetching, indexing
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.memRegPtrIndex(REG_IY, 0);
                        tstates = tstates + 4 + 5; // prefix fetching, index fetching, indexing
                    } else {
                        op1 = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (code == 0x3E) {
                    op1 = Operand.reg8(Operand.Reg8.REG_A);
                } else {
                    throw new RuntimeException();
                }

                if (checkedPrefix != prefix1) {
                    return null;
                }

                return new LdInstruction(code, prefix1, op1, Operand.imm8(0), true, tstates);

            } else if ((code >= 0) && (code <= 0x3F) && ((code & 0x07) == 0x02)) {
                int checkedPrefix = -1;
                Operand op1, op2;
                boolean partial;
                if (code == 0x02) {
                    tstates = 7;
                    op1 = Operand.memRegPtr(REG_BC);
                    op2 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = false;
                } else if (code == 0x0A) {
                    tstates = 7;
                    op2 = Operand.memRegPtr(REG_BC);
                    op1 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = false;
                } else if (code == 0x12) {
                    tstates = 7;
                    op1 = Operand.memRegPtr(Operand.Reg16.REG_DE);
                    op2 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = false;
                } else if (code == 0x1A) {
                    tstates = 7;
                    op2 = Operand.memRegPtr(Operand.Reg16.REG_DE);
                    op1 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = false;
                } else if (code == 0x22) {
                    op1 = Operand.memImmPtr(0);
                    tstates = 4 + 6 + 6; // opcode fetch, imm16 fetch, reg16 write
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op2 = Operand.reg16(REG_IX);
                        tstates += 4; // prefix fetch
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op2 = Operand.reg16(REG_IY);
                        tstates += 4; // prefix fetch
                    } else {
                        op2 = Operand.reg16(Operand.Reg16.REG_HL);
                    }
                    partial = true;
                } else if (code == 0x2A) {
                    op2 = Operand.memImmPtr(0);
                    tstates = 4 + 6 + 6; // opcode fetch, imm16 fetch, reg16 read
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg16(REG_IX);
                        tstates += 4; // prefix fetch
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        op1 = Operand.reg16(REG_IY);
                        tstates += 4; // prefix fetch
                    } else {
                        op1 = Operand.reg16(Operand.Reg16.REG_HL);
                    }
                    partial = true;
                } else if (code == 0x32) {
                    op1 = Operand.memImmPtr(0);
                    op2 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = true;
                    tstates = 4 + 6 + 3; // opcode fetch, imm16 fetch, reg8 write
                } else if (code == 0x3A) {
                    op2 = Operand.memImmPtr(0);
                    op1 = Operand.reg8(Operand.Reg8.REG_A);
                    partial = true;
                    tstates = 4 + 6 + 3; // opcode fetch, imm16 fetch, reg8 read
                } else {
                    throw new RuntimeException();
                }
                if (checkedPrefix != prefix1) {
                    return null;
                }
                return new LdInstruction(code, prefix1, op1, op2, partial, tstates);
            } else if ((prefix1 == -1) && (code == 0x01)) {
                tstates = 10;
                return new LdInstruction(code, prefix1, Operand.reg16(REG_BC), Operand.imm16(0), true, tstates);
            } else if ((prefix1 == -1) && (code == 0x11)) {
                tstates = 10;
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_DE), Operand.imm16(0), true, tstates);
            } else if (code == 0x21) {
                tstates = 10;
                Operand op1;
                if (prefix1 == IX_PREFIX) {
                    op1 = Operand.reg16(REG_IX);
                    tstates += 4; // prefix fetch
                } else if (prefix1 == IY_PREFIX) {
                    op1 = Operand.reg16(REG_IY);
                    tstates += 4; // prefix fetch
                } else {
                    op1 = Operand.reg16(Operand.Reg16.REG_HL);
                }
                return new LdInstruction(code, prefix1, op1, Operand.imm16(0), true, tstates);
            } else if ((prefix1 == -1) && (code == 0x31)) {
                tstates = 10;
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_SP), Operand.imm16(0), true, tstates);
            } else if (code == 0xF9) {
                tstates = 6; // opcode fetch, reg16 transfer
                Operand op2;
                if (prefix1 == IX_PREFIX) {
                    op2 = Operand.reg16(REG_IX);
                    tstates += 4; // prefix fetch
                } else if (prefix1 == IY_PREFIX) {
                    op2 = Operand.reg16(REG_IY);
                    tstates += 4; // prefix fetch
                } else {
                    op2 = Operand.reg16(Operand.Reg16.REG_HL);
                }
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_SP), op2, false, tstates);
            } else {
                return null;
            }
        } else if (prefix1 == 0xED) {
            tstates = 4 + 4 + 6 + 6; // prefix fetch, opcode fetch, imm16 fetch, reg16 read or write
            if (code == 0x43) {
                return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_BC), false, tstates);
            } else if (code == 0x53) {
                return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_DE), false, tstates);
            } else if (code == 0x63) {
                return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_HL), false, tstates);
            } else if (code == 0x73) {
                return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_SP), false, tstates);
            } else if (code == 0x4B) {
                return new LdInstruction(code, prefix1, Operand.reg16(REG_BC), Operand.memImmPtr(0), false, tstates);
            } else if (code == 0x5B) {
                return new LdInstruction(code, prefix1, Operand.reg16(REG_DE), Operand.memImmPtr(0), false, tstates);
            } else if (code == 0x6B) {
                return new LdInstruction(code, prefix1, Operand.reg16(REG_HL), Operand.memImmPtr(0), false, tstates);
            } else if (code == 0x7B) {
                return new LdInstruction(code, prefix1, Operand.reg16(REG_SP), Operand.memImmPtr(0), false, tstates);
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (op2.type == Operand.Type.IMM16 || op2.type == Operand.Type.MEM_PTR_IMM16) {
            op2.imm = decodeImm16(data, offset);
            offset += 2;
        } else if (op2.type == Operand.Type.IMM8) {
            op2.imm = (data[offset] & 0xFF);
            offset++;
        } else if (op2.type == Operand.Type.MEM_PTR_REG) {
            if ((op2.reg16 == REG_IX) || (op2.reg16 == REG_IY)) {
                op2.indexOffset = data[offset];
                offset++;
            }
        } else if ((op2.type == Operand.Type.REG8) || (op2.type == Operand.Type.REG16)) {
            if (op1.type == Operand.Type.MEM_PTR_IMM16) {
                op1.imm = decodeImm16(data, offset);
                offset += 2;
            } else if (op1.type == Operand.Type.MEM_PTR_REG) {
                if ((op1.reg16 == REG_IX) || (op1.reg16 == REG_IY)) {
                    op1.indexOffset = data[offset];
                    offset++;
                }
            }
        }
        return offset;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset =  encodeOperation(dest, offset);
        if (op2.type == Operand.Type.IMM16 || op2.type == Operand.Type.MEM_PTR_IMM16) {
            offset = encodeImm16(dest, offset, op2.imm);
        } else if (op2.type == Operand.Type.IMM8) {
            dest[offset] = (byte)(op2.imm & 0xFF);
            offset++;
        } else if (op2.type == Operand.Type.MEM_PTR_REG) {
            if ((op2.reg16 == REG_IX) || (op2.reg16 == REG_IY)) {
                dest[offset] = (byte)op2.indexOffset;
                offset++;
            }
        } else if ((op2.type == Operand.Type.REG8) || (op2.type == Operand.Type.REG16)) {
            if (op1.type == Operand.Type.MEM_PTR_IMM16) {
                offset = encodeImm16(dest, offset, op1.imm);
            } else if (op1.type == Operand.Type.MEM_PTR_REG) {
                if ((op1.reg16 == REG_IX) || (op1.reg16 == REG_IY)) {
                    dest[offset] = (byte)op2.indexOffset;
                    offset++;
                }
            }
        }
        return offset;
    }



    @Override
    public int getSize() {
        int size = getOperationSize();
        if (op2.type == Operand.Type.IMM16 || op2.type == Operand.Type.MEM_PTR_IMM16) {
            return size + 2;
        } else if (op2.type == Operand.Type.IMM8) {
            return size + 1;
        } else if (op1.type == Operand.Type.MEM_PTR_IMM16) {
            return size + 2;
        }
        return size;

    }

    @Override
    public String getMnemonic() {
        return "LD";
    }

    public String[] getAllMnemonics() {
        return new String[] {"LD"};
    }


}
