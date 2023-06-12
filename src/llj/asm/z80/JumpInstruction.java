package llj.asm.z80;

import static llj.asm.z80.Instruction.Operand.Reg16.*;

public class JumpInstruction extends ControlTransferInstruction {

    public enum Type { RELATIVE, ABS_IMM, ABS_REGISTER }
    public Type type;
    public int offset;
    public int absAddr;
    public Operand.Reg16 reg16;

    public Condition condition;

    public static JumpInstruction relative(int val, Condition condition) throws IncorrectOperandException {
        return new JumpInstruction(Type.RELATIVE, val, null, condition);
    }

    public static JumpInstruction absolute(int val, Condition condition) throws IncorrectOperandException {
        return new JumpInstruction(Type.ABS_IMM, val, null, condition);
    }

    public static JumpInstruction reg16(Instruction.Operand.Reg16 reg16, Condition condition) throws IncorrectOperandException {
        return new JumpInstruction(Type.ABS_REGISTER, 0, reg16, condition);
    }

    private JumpInstruction(Type type, int val, Instruction.Operand.Reg16 reg16, Condition condition) throws IncorrectOperandException {
        this.type = type;
        if (type == Type.RELATIVE) {
            this.offset = val;
        } else if (type == Type.ABS_IMM) {
            this.absAddr = val;
        } else if (type == Type.ABS_REGISTER) {
            this.reg16 = reg16;
        }
        this.condition = condition;
        getCode(type, reg16, condition);
    }

    private JumpInstruction(int code, int prefix1, Type type, int val, Instruction.Operand.Reg16 reg16, Condition condition) {
        this.opCode = code;
        this.prefix1 = prefix1;
        this.type = type;
        if (type == Type.RELATIVE) {
            this.offset = val;
        } else if (type == Type.ABS_IMM) {
            this.absAddr = val;
        } else if (type == Type.ABS_REGISTER) {
            this.reg16 = reg16;
        }
        this.condition = condition;
    }

    private void getCode(Type type, Instruction.Operand.Reg16 reg16, Condition condition) throws IncorrectOperandException {
        int code, prefix1 = -1;
        if (type == Type.RELATIVE) {
            if (condition == null) {
                code = 0x18;
            } else {
                if (condition.flag == ConditionFlag.Z) {
                    code = 0x20;
                } else if (condition.flag == ConditionFlag.C) {
                    code = 0x30;
                } else {
                    throw new IncorrectOperandException();
                }
                code = code + (condition.set ? 0x08 : 0);
            }
        } else if (type == Type.ABS_IMM) {
            if (condition == null) {
                code = 0xC3;
            } else {
                if (condition.flag == ConditionFlag.Z) {
                    code = 0xC2;
                } else if (condition.flag == ConditionFlag.C) {
                    code = 0xD2;
                } else if (condition.flag == ConditionFlag.PV) {
                    code = 0xE2;
                } else if (condition.flag == ConditionFlag.S) {
                    code = 0xF2;
                } else {
                    throw new RuntimeException();
                }
                code = code + (condition.set ? 0x08 : 0);
            }
        }  else if (type == Type.ABS_REGISTER) {
            if (condition != null) {
                throw new IncorrectOperandException();
            }
            if (reg16 == REG_HL) {
                code = 0xE9;
            } else if (reg16 == REG_IX) {
                prefix1 = IX_PREFIX;
                code = 0xE9;
            } else if (reg16 == Operand.Reg16.REG_IY) {
                prefix1 = IY_PREFIX;
                code = 0xE9;
            } else {
                throw new IncorrectOperandException();
            }
        } else {
            throw new RuntimeException();
        }
        this.opCode = code;
        this.prefix1 = prefix1;
    }

    public static JumpInstruction decodeInitial(int code, int prefix1) {
        if (code == 0xC3) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, null);
        } else if (code == 0xC2) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.NZ);
        } else if (code == 0xD2) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.NC);
        } else if (code == 0xE2) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.PO);
        } else if (code == 0xF2) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.P);
        } else if (code == 0xCA) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.Z);
        } else if (code == 0xDA) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.C);
        } else if (code == 0xEA) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.PE);
        } else if (code == 0xFA) {
            return new JumpInstruction(code, prefix1, Type.ABS_IMM, 0, null, Condition.M);
        } else if (code == 0x18) {
            return new JumpInstruction(code, prefix1, Type.RELATIVE, 0, null, null);
        } else if (code == 0x20) {
            return new JumpInstruction(code, prefix1, Type.RELATIVE, 0, null, Condition.NZ);
        } else if (code == 0x30) {
            return new JumpInstruction(code, prefix1, Type.RELATIVE, 0, null, Condition.NC);
        } else if (code == 0x28) {
            return new JumpInstruction(code, prefix1, Type.RELATIVE, 0, null, Condition.Z);
        } else if (code == 0x38) {
            return new JumpInstruction(code, prefix1, Type.RELATIVE, 0, null, Condition.C);
        } else if (code == 0xE9) {
            Operand.Reg16 reg16;
            if (prefix1 == IX_PREFIX) {
                reg16 = REG_IX;
            } else if (prefix1 == IY_PREFIX) {
                reg16 = REG_IY;
            } else {
                reg16 = REG_HL;
            }
            return new JumpInstruction(code, prefix1, Type.ABS_REGISTER, 0, reg16, null);
        } else {
            return null;
        }
    }

    @Override
    public String getMnemonic() {
        return type == Type.RELATIVE ? "JR " : "JP " + ((condition != null) ? condition.toString() : "");
    }

    public String[] getAllMnemonics() {
        return new String[] {"JP", "JR"};
    }

    @Override
    public int decodeRemaining(byte[] data, int offset) {
        if (type == Type.RELATIVE) {
            this.offset = data[offset];
            offset++;
        } else if (type == Type.ABS_REGISTER) {
            // do nothing
        } else if (type == Type.ABS_IMM) {
            this.absAddr = decodeImm16(data, offset);
            offset += 2;
        } else {
            throw new RuntimeException();
        }
        return offset;
    }

    @Override
    public int getSize() {
        if (type == Type.RELATIVE) {
            return getOperationSize() + 1;
        } else if (type == Type.ABS_REGISTER) {
            return getOperationSize();
        } else if (type == Type.ABS_IMM) {
            return getOperationSize() + 2;
        } else {
            throw new RuntimeException();
        }

    }

    @Override
    public int encode(byte[] dest, int offset) {
        offset = encodeOperation(dest, offset);
        if (type == Type.RELATIVE) {
            dest[offset] = (byte)this.offset;
            offset++;
        } else if (type == Type.ABS_REGISTER) {
            // do nothing
        } else if (type == Type.ABS_IMM) {
            offset = encodeImm16(dest, offset, this.absAddr);
        } else {
            throw new RuntimeException();
        }

        return offset;
    }
}
