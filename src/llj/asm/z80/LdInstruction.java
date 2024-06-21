package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.*;
import static llj.asm.z80.Instruction.Operand.Reg8.REG_A;
import static llj.asm.z80.Instruction.Operand.Reg8.REG_I;

public class LdInstruction extends Instruction {

    public final Operand opDest, opSrc;

    public LdInstruction(Operand opDest, Operand opSrc) throws IncorrectOperandException {
        this.opDest = opDest;
        this.opSrc =opSrc;
        setOpCode(opDest, opSrc);
    }

    public LdInstruction(int opCode, int prefix1, Operand opDest, Operand opSrc, boolean partial) {
        this.opDest = opDest;
        this.opSrc =opSrc;
        this.opCode = opCode;
        this.prefix1 = prefix1;
    }

    private void setOpCode(Operand opDest, Operand opSrc) throws IncorrectOperandException {
        int code, prefix1 = -1;

        if (opDest.type == Operand.Type.REG16) {
            if (opSrc.type == Operand.Type.IMM16) {
                if (opDest.reg16 == REG_BC) {
                    code = 0x01;
                } else if (opDest.reg16 == Operand.Reg16.REG_DE) {
                    code = 0x11;
                } else if (opDest.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x21;
                } else if (opDest.reg16 == REG_IX) {
                    prefix1 = IX_PREFIX;
                    code = 0x21;
                } else if (opDest.reg16 == REG_IY) {
                    prefix1 = IY_PREFIX;
                    code = 0x21;
                } else if (opDest.reg16 == Operand.Reg16.REG_SP) {
                    code = 0x31;
                } else {
                    throw new RuntimeException();
                }
            } else if (opSrc.type == Operand.Type.REG16) {
                if ((opDest.reg16 == Operand.Reg16.REG_SP) && (opSrc.reg16 == Operand.Reg16.REG_HL)) {
                    code = 0xF9;
                } else if ((opDest.reg16 == Operand.Reg16.REG_SP) && (opSrc.reg16 == REG_IX)) {
                    prefix1 = IX_PREFIX;
                    code = 0xF9;
                } else if ((opDest.reg16 == Operand.Reg16.REG_SP) && (opSrc.reg16 == REG_IY)) {
                    prefix1 = IY_PREFIX;
                    code = 0xF9;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (opSrc.type == Operand.Type.MEM_PTR_IMM16) {
                if (opDest.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x2A; // only HL pair has two choices: either short opcode 0x2A or longer 0xED 0x6B
                } else if (opDest.reg16 == REG_IX) {
                    prefix1 = IX_PREFIX;
                    code = 0x2A;
                } else if (opDest.reg16 == REG_IY) {
                    prefix1 = IY_PREFIX;
                    code = 0x2A;
                } else if (opDest.reg16 == REG_BC) {
                    prefix1 = 0xED;
                    code = 0x4B;
                } else if (opDest.reg16 == Operand.Reg16.REG_DE) {
                    prefix1 = 0xED;
                    code = 0x5B;
                } else if (opDest.reg16 == Operand.Reg16.REG_SP) {
                    prefix1 = 0xED;
                    code = 0x7B;
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new RuntimeException();
            }
        } else if ((opDest.type == Operand.Type.REG8) || (opDest.type == Operand.Type.MEM_PTR_REG16)) {

            if ((opSrc.type == Operand.Type.REG8) || (opSrc.type == Operand.Type.MEM_PTR_REG16)) {

                if (opDest.reg8 == Operand.Reg8.REG_B) {
                    code = 0x40;
                } else if (opDest.reg8 == Operand.Reg8.REG_C) {
                    code = 0x48;
                } else if (opDest.reg8 == Operand.Reg8.REG_D) {
                    code = 0x50;
                } else if (opDest.reg8 == Operand.Reg8.REG_E) {
                    code = 0x58;
                } else if (opDest.reg8 == Operand.Reg8.REG_H) {
                    code = 0x60;
                } else if (opDest.reg8 == Operand.Reg8.REG_IXH) {
                    prefix1 = IX_PREFIX;
                    code = 0x60;
                } else if (opDest.reg8 == Operand.Reg8.REG_IYH) {
                    prefix1 = IY_PREFIX;
                    code = 0x60;
                } else if (opDest.reg8 == Operand.Reg8.REG_L) {
                    code = 0x68;
                } else if (opDest.reg8 == Operand.Reg8.REG_IXL) {
                    prefix1 = IX_PREFIX;
                    code = 0x68;
                } else if (opDest.reg8 == Operand.Reg8.REG_IYL) {
                    prefix1 = IY_PREFIX;
                    code = 0x68;
                } else if (opDest.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x70;
                } else if (opDest.reg16 == REG_IX) {
                    prefix1 = IX_PREFIX;
                    code = 0x70;
                } else if (opDest.reg16 == REG_IY) {
                    prefix1 = IY_PREFIX;
                    code = 0x70;
                } else if (opDest.reg8 == REG_A) {
                    code = 0x78;
                } else if (opDest.reg16 == REG_BC) {
                    code = 0x02;
                } else if (opDest.reg16 == Operand.Reg16.REG_DE) {
                    code = 0x12;
                } else {
                    throw new RuntimeException();
                }


                // Special handling for LD (BC),A and LD (DE),A
                if ((opDest.type == Operand.Type.MEM_PTR_REG16) && (opDest.reg16 == REG_BC)) {
                    if (opSrc.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    // don't need to update a code, it's already correct

                } else if ((opDest.type == Operand.Type.MEM_PTR_REG16) && (opDest.reg16 == Operand.Reg16.REG_DE)) {
                    if (opSrc.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    // don't need to update a code, it's already correct

                } else if ((opDest.type == Operand.Type.REG8) && (opDest.reg8 == Operand.Reg8.REG_R)) {
                    if (opSrc.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    prefix1 = 0xED;
                    code = 0x4F;
                } else if ((opDest.type == Operand.Type.REG8) && (opDest.reg8 == REG_I)) {
                    if (opSrc.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    prefix1 = 0xED;
                    code = 0x47;
                } else if ((opSrc.type == Operand.Type.REG8) && (opSrc.reg8 == Operand.Reg8.REG_R)) {
                    if (opDest.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    prefix1 = 0xED;
                    code = 0x5F;
                } else if ((opSrc.type == Operand.Type.REG8) && (opSrc.reg8 == REG_I)) {
                    if (opDest.reg8 != REG_A) {
                        throw new IncorrectOperandException();
                    }
                    prefix1 = 0xED;
                    code = 0x57;

                } else {

                    if (opSrc.reg8 == Operand.Reg8.REG_B) {
                        code += 0x00;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_C) {
                        code += 0x01;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_D) {
                        code += 0x02;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_E) {
                        code += 0x03;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_H) {
                        if ((opDest.reg8 == Operand.Reg8.REG_IXH) || (opDest.reg8 == Operand.Reg8.REG_IXL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x04;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_IXH) {
                        if ((opDest.reg8 == Operand.Reg8.REG_H) || (opDest.reg8 == Operand.Reg8.REG_L) || (opDest.reg16 == Operand.Reg16.REG_HL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IX_PREFIX;
                        code += 0x04;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_IYH) {
                        if ((opDest.reg8 == Operand.Reg8.REG_H) || (opDest.reg8 == Operand.Reg8.REG_L) || (opDest.reg16 == Operand.Reg16.REG_HL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IY_PREFIX;
                        code += 0x04;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_L) {
                        if ((opDest.reg8 == Operand.Reg8.REG_IXH) || (opDest.reg8 == Operand.Reg8.REG_IXL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x05;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_IXL) {
                        if ((opDest.reg8 == Operand.Reg8.REG_H) || (opDest.reg8 == Operand.Reg8.REG_L) || (opDest.reg16 == Operand.Reg16.REG_HL)  || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IX_PREFIX;
                        code += 0x05;
                    } else if (opSrc.reg8 == Operand.Reg8.REG_IYL) {
                        if ((opDest.reg8 == Operand.Reg8.REG_H) || (opDest.reg8 == Operand.Reg8.REG_L) || (opDest.reg16 == Operand.Reg16.REG_HL)  || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IY_PREFIX;
                        code += 0x05;
                    } else if (opSrc.reg16 == Operand.Reg16.REG_HL) {
                        if (opDest.type == Operand.Type.MEM_PTR_REG16) {
                            throw new IncorrectOperandException();
                        } else if ((opDest.reg8 == Operand.Reg8.REG_IXH) || (opDest.reg8 == Operand.Reg8.REG_IXL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        code += 0x06;
                    } else if (opSrc.reg16 == REG_IX) {
                        if (opDest.type == Operand.Type.MEM_PTR_REG16) {
                            throw new IncorrectOperandException();
                        } else if ((opDest.reg8 == Operand.Reg8.REG_IXH) || (opDest.reg8 == Operand.Reg8.REG_IXL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IX_PREFIX;
                        code += 0x06;
                    } else if (opSrc.reg16 == REG_IY) {
                        if (opDest.type == Operand.Type.MEM_PTR_REG16) {
                            throw new IncorrectOperandException();
                        } else if ((opDest.reg8 == Operand.Reg8.REG_IXH) || (opDest.reg8 == Operand.Reg8.REG_IXL) || (opDest.reg8 == Operand.Reg8.REG_IYH) || (opDest.reg8 == Operand.Reg8.REG_IYL)) {
                            throw new IncorrectOperandException();
                        }
                        prefix1 = IY_PREFIX;
                        code += 0x06;
                    } else if (opSrc.reg8 == REG_A) {
                        code += 0x07;
                    } else if (opSrc.reg16 == REG_BC) {
                        if ((opDest.type == Operand.Type.REG8) && (opDest.reg8 == REG_A)) {
                            code = 0x0A;
                        } else {
                            throw new IncorrectOperandException();
                        }
                    } else if (opSrc.reg16 == Operand.Reg16.REG_DE) {
                        if ((opDest.type == Operand.Type.REG8) && (opDest.reg8 == REG_A)) {
                            code = 0x1A;
                        } else {
                            throw new IncorrectOperandException();
                        }
                    } else {
                        throw new IncorrectOperandException();
                    }
                }
            } else if (opSrc.type == Operand.Type.IMM8) {
                if (opDest.reg8 == Operand.Reg8.REG_B) {
                    code = 0x06;
                } else if (opDest.reg8 == Operand.Reg8.REG_C) {
                    code = 0x0E;
                } else if (opDest.reg8 == Operand.Reg8.REG_D) {
                    code = 0x16;
                } else if (opDest.reg8 == Operand.Reg8.REG_E) {
                    code = 0x1E;
                } else if (opDest.reg8 == Operand.Reg8.REG_H) {
                    code = 0x26;
                } else if (opDest.reg8 == Operand.Reg8.REG_IXH) {
                    prefix1 = IX_PREFIX;
                    code = 0x26;
                } else if (opDest.reg8 == Operand.Reg8.REG_IYH) {
                    prefix1 = IY_PREFIX;
                    code = 0x26;
                } else if (opDest.reg8 == Operand.Reg8.REG_L) {
                    code = 0x2E;
                } else if (opDest.reg8 == Operand.Reg8.REG_IXL) {
                    prefix1 = IX_PREFIX;
                    code = 0x2E;
                } else if (opDest.reg8 == Operand.Reg8.REG_IYL) {
                    prefix1 = IY_PREFIX;
                    code = 0x2E;
                } else if (opDest.reg16 == Operand.Reg16.REG_HL) {
                    code = 0x36;
                } else if (opDest.reg16 == REG_IX) {
                    prefix1 = IX_PREFIX;
                    code = 0x36;
                } else if (opDest.reg16 == REG_IY) {
                    prefix1 = IY_PREFIX;
                    code = 0x36;
                } else if (opDest.reg8 == REG_A) {
                    code = 0x3E;
                } else {
                    throw new IncorrectOperandException();
                }
            } else if (opSrc.type == Operand.Type.MEM_PTR_IMM16) {
                if ((opDest.type == Operand.Type.REG8) && (opDest.reg8 == REG_A)) {
                    code = 0x3A;
                } else {
                    throw new IncorrectOperandException();
                }
            } else {
                throw new RuntimeException();
            }
        } else if (opDest.type == Operand.Type.MEM_PTR_IMM16) {
            if ((opSrc.type == Operand.Type.REG8) && (opSrc.reg8 == REG_A)) {
                code = 0x32;
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == Operand.Reg16.REG_HL)) {
                // LD (NN),HL
                code = 0x22; // only HL pair has two choices: either short opcode 0x22 or longer 0xED 0x63
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == REG_IX)) {
                prefix1 = IX_PREFIX;
                code = 0x22;
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == REG_IY)) {
                prefix1 = IY_PREFIX;
                code = 0x22;
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == REG_BC)) {
                prefix1 = 0xED;
                code = 0x43;
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == Operand.Reg16.REG_DE)) {
                prefix1 = 0xED;
                code = 0x53;
            } else if ((opSrc.type == Operand.Type.REG16) && (opSrc.reg16 == Operand.Reg16.REG_SP)) {
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
    }

    public static LdInstruction decodeInitial(int code, int prefix1) {
        prefix1 = prefix1;
        code = code & 0xFF;

        if ((prefix1 == -1) || (prefix1 == IX_PREFIX) || (prefix1 == IY_PREFIX)) {
            if ((code >= 0x40) && (code <= 0x7F)) {
                if (code == 0x76) {
                    return null; // HALT code
                }

                // I'm too lazy to check prefix to be empty every time, so this will be default value to be checked at the end
                int checkedPrefix = -1;

                int src = code & 0x07;
                int dst = code & 0x78;

                Operand opDest, opSrc;
                if (dst == 0x40) {
                    opDest = Operand.reg8(Operand.Reg8.REG_B);
                } else if (dst == 0x48) {
                    opDest = Operand.reg8(Operand.Reg8.REG_C);
                } else if (dst == 0x50) {
                    opDest = Operand.reg8(Operand.Reg8.REG_D);
                } else if (dst == 0x58) {
                    opDest = Operand.reg8(Operand.Reg8.REG_E);
                } else if (dst == 0x60) {
                    if ((prefix1 == IX_PREFIX) && (src != 0x06)){
                        checkedPrefix = prefix1;
                        // in this case prefix is related to opSrc
                        opDest = Operand.reg8(Operand.Reg8.REG_IXH);
                    } else if ((prefix1 == IY_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        // in this case prefix is related to opSrc
                        opDest = Operand.reg8(Operand.Reg8.REG_IYH);
                    } else {
                        opDest = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (dst == 0x68) {
                    if ((prefix1 == IX_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        // in this case prefix is related to opSrc
                        opDest = Operand.reg8(Operand.Reg8.REG_IXL);
                    } else if ((prefix1 == IY_PREFIX) && (src != 0x06)) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg8(Operand.Reg8.REG_IYL);
                    } else {
                        opDest = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (dst == 0x70) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.memRegPtrIndex(REG_IX, 0);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.memRegPtrIndex(REG_IY, 0);
                    } else {
                        opDest = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (dst == 0x78) {
                    opDest = Operand.reg8(REG_A);
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
                    if ((prefix1 == IX_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        opSrc =  Operand.reg8(Operand.Reg8.REG_IXH);
                    } else if ((prefix1 == IY_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.reg8(Operand.Reg8.REG_IYH);
                    } else {
                        opSrc = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (src == 0x05) {
                    if ((prefix1 == IX_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.reg8(Operand.Reg8.REG_IXL);
                    } else if ((prefix1 == IY_PREFIX) && (dst != 0x70)) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.reg8(Operand.Reg8.REG_IYL);
                    } else {
                        opSrc = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (src == 0x06) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.memRegPtrIndex(REG_IX, 0);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.memRegPtrIndex(REG_IY, 0);
                    } else {
                        opSrc = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (src == 0x07) {
                    opSrc = Operand.reg8(REG_A);
                } else {
                    throw new RuntimeException();
                }

                if (checkedPrefix != prefix1) {
                    return null;
                }

                return new LdInstruction(code, prefix1, opDest, opSrc, false);
            } else if ((code >= 0) && (code <= 0x3F) && ((code & 0x07) == 0x06)) {
                int checkedPrefix = -1;
                Operand opDest;
                if (code == 0x06) {
                    opDest = Operand.reg8(Operand.Reg8.REG_B);
                } else if (code == 0x0E) {
                    opDest = Operand.reg8(Operand.Reg8.REG_C);
                } else if (code == 0x16) {
                    opDest = Operand.reg8(Operand.Reg8.REG_D);
                } else if (code == 0x1E) {
                    opDest = Operand.reg8(Operand.Reg8.REG_E);
                } else if (code == 0x26) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg8(Operand.Reg8.REG_IXH);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg8(Operand.Reg8.REG_IYH);
                    } else {
                        opDest = Operand.reg8(Operand.Reg8.REG_H);
                    }
                } else if (code == 0x2E) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg8(Operand.Reg8.REG_IXL);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg8(Operand.Reg8.REG_IYL);
                    } else {
                        opDest = Operand.reg8(Operand.Reg8.REG_L);
                    }
                } else if (code == 0x36) {
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.memRegPtrIndex(REG_IX, 0);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.memRegPtrIndex(REG_IY, 0);
                    } else {
                        opDest = Operand.memRegPtr(Operand.Reg16.REG_HL);
                    }
                } else if (code == 0x3E) {
                    opDest = Operand.reg8(REG_A);
                } else {
                    throw new RuntimeException();
                }

                if (checkedPrefix != prefix1) {
                    return null;
                }

                return new LdInstruction(code, prefix1, opDest, Operand.imm8(0), true);

            } else if ((code >= 0) && (code <= 0x3F) && ((code & 0x07) == 0x02)) {
                int checkedPrefix = -1;
                Operand opDest, opSrc;
                boolean partial;
                if (code == 0x02) {
                    opDest = Operand.memRegPtr(REG_BC);
                    opSrc = Operand.reg8(REG_A);
                    partial = false;
                } else if (code == 0x0A) {
                    opSrc = Operand.memRegPtr(REG_BC);
                    opDest = Operand.reg8(REG_A);
                    partial = false;
                } else if (code == 0x12) {
                    opDest = Operand.memRegPtr(Operand.Reg16.REG_DE);
                    opSrc = Operand.reg8(REG_A);
                    partial = false;
                } else if (code == 0x1A) {
                    opSrc = Operand.memRegPtr(Operand.Reg16.REG_DE);
                    opDest = Operand.reg8(REG_A);
                    partial = false;
                } else if (code == 0x22) {
                    opDest = Operand.memImmPtr(0);
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.reg16(REG_IX);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opSrc = Operand.reg16(REG_IY);
                    } else {
                        opSrc = Operand.reg16(Operand.Reg16.REG_HL);
                    }
                    partial = true;
                } else if (code == 0x2A) {
                    opSrc = Operand.memImmPtr(0);
                    if (prefix1 == IX_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg16(REG_IX);
                    } else if (prefix1 == IY_PREFIX) {
                        checkedPrefix = prefix1;
                        opDest = Operand.reg16(REG_IY);
                    } else {
                        opDest = Operand.reg16(Operand.Reg16.REG_HL);
                    }
                    partial = true;
                } else if (code == 0x32) {
                    opDest = Operand.memImmPtr(0);
                    opSrc = Operand.reg8(REG_A);
                    partial = true;
                } else if (code == 0x3A) {
                    opSrc = Operand.memImmPtr(0);
                    opDest = Operand.reg8(REG_A);
                    partial = true;
                } else {
                    throw new RuntimeException();
                }
                if (checkedPrefix != prefix1) {
                    return null;
                }
                return new LdInstruction(code, prefix1, opDest, opSrc, partial);
            } else if ((prefix1 == -1) && (code == 0x01)) {
                return new LdInstruction(code, prefix1, Operand.reg16(REG_BC), Operand.imm16(0), true);
            } else if ((prefix1 == -1) && (code == 0x11)) {
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_DE), Operand.imm16(0), true);
            } else if (code == 0x21) {
                Operand opDest;
                if (prefix1 == IX_PREFIX) {
                    opDest = Operand.reg16(REG_IX);
                } else if (prefix1 == IY_PREFIX) {
                    opDest = Operand.reg16(REG_IY);
                } else {
                    opDest = Operand.reg16(Operand.Reg16.REG_HL);
                }
                return new LdInstruction(code, prefix1, opDest, Operand.imm16(0), true);
            } else if ((prefix1 == -1) && (code == 0x31)) {
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_SP), Operand.imm16(0), true);
            } else if (code == 0xF9) {
                Operand opSrc;
                if (prefix1 == IX_PREFIX) {
                    opSrc = Operand.reg16(REG_IX);
                } else if (prefix1 == IY_PREFIX) {
                    opSrc = Operand.reg16(REG_IY);
                } else {
                    opSrc = Operand.reg16(Operand.Reg16.REG_HL);
                }
                return new LdInstruction(code, prefix1, Operand.reg16(Operand.Reg16.REG_SP), opSrc, false);
            } else {
                return null;
            }
        } else if (prefix1 == 0xED) {
            if (code == 0x47) {
                return new LdInstruction(code, prefix1, Operand.reg8(Operand.Reg8.REG_I), Operand.reg8(Operand.Reg8.REG_A), false);
            } else if (code == 0x4F) {
                return new LdInstruction(code, prefix1, Operand.reg8(Operand.Reg8.REG_R), Operand.reg8(Operand.Reg8.REG_A), false);
            } else if (code == 0x57) {
                return new LdInstruction(code, prefix1, Operand.reg8(Operand.Reg8.REG_A), Operand.reg8(Operand.Reg8.REG_I), false);
            } else if (code == 0x5F) {
                return new LdInstruction(code, prefix1, Operand.reg8(Operand.Reg8.REG_A), Operand.reg8(Operand.Reg8.REG_R), false);
            } else {
                if (code == 0x43) {
                    return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_BC), false);
                } else if (code == 0x53) {
                    return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_DE), false);
                } else if (code == 0x63) {
                    return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_HL), false);
                } else if (code == 0x73) {
                    return new LdInstruction(code, prefix1, Operand.memImmPtr(0), Operand.reg16(REG_SP), false);
                } else if (code == 0x4B) {
                    return new LdInstruction(code, prefix1, Operand.reg16(REG_BC), Operand.memImmPtr(0), false);
                } else if (code == 0x5B) {
                    return new LdInstruction(code, prefix1, Operand.reg16(REG_DE), Operand.memImmPtr(0), false);
                } else if (code == 0x6B) {
                    return new LdInstruction(code, prefix1, Operand.reg16(REG_HL), Operand.memImmPtr(0), false);
                } else if (code == 0x7B) {
                    return new LdInstruction(code, prefix1, Operand.reg16(REG_SP), Operand.memImmPtr(0), false);
                } else {
                    return null;
                }
            }

        } else {
            return null;
        }
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (opSrc.type == Operand.Type.IMM16 || opSrc.type == Operand.Type.MEM_PTR_IMM16) {
            opSrc.imm = decodeImm16(data, offset);
            offset += 2;
        } else if (opSrc.type == Operand.Type.IMM8) {
            if (opDest.type == Operand.Type.MEM_PTR_REG16 && (opDest.reg16 == REG_IX) || (opDest.reg16 == REG_IY)) {
                opDest.indexOffset = data[offset];
                offset++;
            }
            opSrc.imm = (data[offset] & 0xFF);
            offset++;
        } else if (opSrc.type == Operand.Type.MEM_PTR_REG16) {
            if ((opSrc.reg16 == REG_IX) || (opSrc.reg16 == REG_IY)) {
                opSrc.indexOffset = data[offset];
                offset++;
            }
        } else if ((opSrc.type == Operand.Type.REG8) || (opSrc.type == Operand.Type.REG16)) {
            if (opDest.type == Operand.Type.MEM_PTR_IMM16) {
                opDest.imm = decodeImm16(data, offset);
                offset += 2;
            } else if (opDest.type == Operand.Type.MEM_PTR_REG16) {
                if ((opDest.reg16 == REG_IX) || (opDest.reg16 == REG_IY)) {
                    opDest.indexOffset = data[offset];
                    offset++;
                }
            }
        }
        return offset;
    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset =  encodeOperation(dest, offset);
        if (opSrc.type == Operand.Type.IMM16 || opSrc.type == Operand.Type.MEM_PTR_IMM16) {
            offset = encodeImm16(dest, offset, opSrc.imm);
        } else if (opSrc.type == Operand.Type.IMM8) {
            dest[offset] = (byte)(opSrc.imm & 0xFF);
            offset++;
        } else if (opSrc.type == Operand.Type.MEM_PTR_REG16) {
            if ((opSrc.reg16 == REG_IX) || (opSrc.reg16 == REG_IY)) {
                dest[offset] = (byte) opSrc.indexOffset;
                offset++;
            }
        } else if ((opSrc.type == Operand.Type.REG8) || (opSrc.type == Operand.Type.REG16)) {
            if (opDest.type == Operand.Type.MEM_PTR_IMM16) {
                offset = encodeImm16(dest, offset, opDest.imm);
            } else if (opDest.type == Operand.Type.MEM_PTR_REG16) {
                if ((opDest.reg16 == REG_IX) || (opDest.reg16 == REG_IY)) {
                    dest[offset] = (byte) opSrc.indexOffset;
                    offset++;
                }
            }
        }
        return offset;
    }



    @Override
    public int getSize() {
        int size = getOperationSize();
        if (opSrc.type == Operand.Type.IMM16 || opSrc.type == Operand.Type.MEM_PTR_IMM16) {
            return size + 2;
        } else if (opSrc.type == Operand.Type.IMM8) {
            return size + 1;
        } else if (opDest.type == Operand.Type.MEM_PTR_IMM16) {
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
