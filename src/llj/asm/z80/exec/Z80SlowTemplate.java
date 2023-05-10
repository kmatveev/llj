package llj.asm.z80.exec;

import llj.asm.z80.Instruction;
import llj.asm.z80.Op2Instruction;

import static llj.asm.z80.Instruction.IX_PREFIX;
import static llj.asm.z80.Instruction.IY_PREFIX;

public class Z80SlowTemplate implements Z80Template {

    private int regA, regB, regC, regD, regH, regL, regIX, regIY, regSP;
    private int pc;

    private int tstates;

    // Flags
    private boolean CF, ZF, SF, PVF, NF, B3F, B5F, HF;

    public static interface Env {
        public byte getMemByte(int addr);
        public void setMemByte(int addr, byte val);
    }
    private Env env;

    private static class SimpleEnv implements Env {
        private byte[] mem = new byte[65536];

        @Override
        public byte getMemByte(int addr) {
            return mem[addr];
        }

        @Override
        public void setMemByte(int addr, byte val) {
            mem[addr] = val;
        }
    }

    public byte fetchFromPC() {
        byte result = env.getMemByte(pc);
        pc++;
        return result;
    }

    public int fetchFromPC2() {
        byte result1 = env.getMemByte(pc);
        pc++;
        byte result2 = env.getMemByte(pc);
        pc++;
        return ((result2 & 0xFF) << 8) | (result1 & 0xFF);
    }

    public int getMemWord(int addr) {
        byte result1 = env.getMemByte(addr);
        byte result2 = env.getMemByte(addr + 1);
        return ((result2 & 0xFF) << 8) | (result1 & 0xFF);
    }

    public void exec(int numTstates) {
        int expectedTstates = tstates + numTstates;

        int code = fetchFromPC();
        tstates += 4;  // for fetching either prefix or opcode
        int prefix1 = -1, prefix2 = -1;
        if (code == IX_PREFIX) {
            prefix1 = code;
            code = fetchFromPC();
            tstates += 4;  // for fetching opcode
        } else if (code == IY_PREFIX) {
            prefix1 = code;
            code = fetchFromPC();
            tstates += 4;  // for fetching opcode
        } else if (code == 0xED) {
            prefix1 = code;
            code = fetchFromPC();
            tstates += 4;  // for fetching opcode
        }

        if (code == 0xCB) {
            if (prefix1 != -1) {
                prefix2 = code;
            } else {
                prefix1 = code;
            }
            code = fetchFromPC();
            tstates += 4;  // for fetching opcode
        }

        int rv = 0, v = 0, vv = 0, vvv = 0; // for temp values

        if (prefix1 == -1) {
            execInstruction1();
        } else if (prefix1 == IX_PREFIX) {
            execInstruction2();
        } else if (prefix1 == IY_PREFIX) {
            execInstruction2();
        } else if (prefix1 == 0xED) {
            execInstruction3();
        } else if (prefix1 == 0xCB) {
            execInstruction4();
        }

    }

    public void execInstruction1() {
        // empty stub
    }

    public void execInstruction2() {
        // empty stub
    }

    public void execInstruction3() {
        // empty stub
    }

    public void execInstruction4() {
        // empty stub
    }

    public static String reg(Instruction.Operand.Reg8 reg8) {
        switch (reg8) {
            case REG_A: return "regA";
            case REG_B: return "regB";
            case REG_C: return "regC";
            case REG_D: return "regD";
            case REG_E: return "regE";
            case REG_H: return "regH";
            case REG_L: return "regL";
            default: throw new RuntimeException();
        }
    }

    public static String reg(Instruction.Operand.Reg16 reg16) {
        switch (reg16) {
            case REG_SP: return "regSP";
            case REG_IX: return "regIX";
            case REG_IY: return "regIY";
            default: throw new RuntimeException();
        }
    }

    @Override
    public String tmpget() {
        return "v";
    }

    @Override
    public String tmpset(String val) {
        return "v = " + val + "; ";
    }

    @Override
    public String tmp2get() {
        return "vv";
    }

    @Override
    public String tmp2set(String val) {
        return "vv = " + val + "; ";
    }

    @Override
    public String tmp3get() {
        return "vvv";
    }

    @Override
    public String tmp3set(String val) {
        return "vvv = " + val + "; ";
    }

    @Override
    public String reg8set(Instruction.Operand.Reg8 reg8, String val) {
        return reg(reg8) + " = " + val + "; ";
    }

    @Override
    public String memset8(String addr, String val) {
        return "env.setMemByte(" + addr + "," + val + "); ";
    }

    @Override
    public String memget8(String addr) {
        return "env.getMemByte(" + addr + ")";
    }

    @Override
    public String memget16(String addr) {
        return "getMemWord(" + addr + ")";
    }

    @Override
    public String memset16(String addr, String val) {
        return "rv = " + val + "; env.setMemByte(" + addr + " , (rv & 0xFF) << 8); env.setMemByte(" + addr + ", (rv & 0xFF)); ";
    }

    @Override
    public String reg8get(Instruction.Operand.Reg8 reg8) {
        return reg(reg8);
    }

    @Override
    public String reg16get(Instruction.Operand.Reg16 reg16) {
        if ((reg16 == Instruction.Operand.Reg16.REG_SP) || (reg16 == Instruction.Operand.Reg16.REG_IX) || (reg16 == Instruction.Operand.Reg16.REG_IY)) {
            return reg(reg16);
        } else {
            Instruction.Operand.Reg8[] pair = Instruction.Operand.split(reg16);
            // ( regH << 8 ) | regL
            return "((" + reg(pair[0]) + " << 8) | " + reg(pair[1]) + ")";
        }
    }

    @Override
    public String reg16set(Instruction.Operand.Reg16 reg16, String val) {
        if ((reg16 == Instruction.Operand.Reg16.REG_SP) || (reg16 == Instruction.Operand.Reg16.REG_IX) || (reg16 == Instruction.Operand.Reg16.REG_IY)) {
            // regSP = val;
            return reg(reg16) + " = " + val + "; ";
        } else {
            Instruction.Operand.Reg8[] pair = Instruction.Operand.split(reg16);
            // v = val; regH = (v >> 8) & 0xFF; regL = v & 0xFF;
            return "rv = " + val + "; " + reg(pair[0]) + " = (rv >> 8) & 0xFF; " + reg(pair[1]) + " = rv & 0xFF" + "; ";
        }
    }

    public String imm8get() {
        return "fetchFromPC()";
    }

    @Override
    public String imm16get() {
        return "fetchFromPC2()";
    }

    @Override
    public String operation8(Op2Instruction.Operation operation, String op1, String op2) {
        String strOp;

        if (operation == Op2Instruction.Operation.ADD) {
            strOp = " + ";
        } else if (operation == Op2Instruction.Operation.ADC) {
            strOp = " + "; // TODO CF
        } else if (operation == Op2Instruction.Operation.SUB) {
            strOp = " - ";
        } else if (operation == Op2Instruction.Operation.SBC) {
            strOp = " - "; // TODO CF
        } else if (operation == Op2Instruction.Operation.AND) {
            strOp = " & ";
        } else if (operation == Op2Instruction.Operation.XOR) {
            strOp = " ^ ";
        } else if (operation == Op2Instruction.Operation.OR) {
            strOp = " | ";
        } else if (operation == Op2Instruction.Operation.CP) {
            strOp = " - "; // TODO ignore
        } else {
            throw new RuntimeException();
        }
        // TODO flags
        return "0xFF & (" + op1 + strOp + op2 + ")";
    }

    @Override
    public String operation16(Op2Instruction.Operation operation, String op1, String op2) {
        String strOp;
        if (operation == Op2Instruction.Operation.ADD) {
            strOp = " + ";
        } else if (operation == Op2Instruction.Operation.ADC) {
            strOp = " + "; // TODO CF
        } else if (operation == Op2Instruction.Operation.SUB) {
            strOp = " - ";
        } else if (operation == Op2Instruction.Operation.SBC) {
            strOp = " - "; // TODO CF
        } else {
            throw new RuntimeException();
        }
        // TODO flags
        return "0xFFFF & (" + op1 + " + " + op2 + ")";
    }

    @Override
    public String flagset(Flag flag, String val) {
        String f;
        if (flag == Flag.CFlag) {
            f = "CF";
        } else if (flag == Flag.ZFlag) {
            f = "ZF";
        } else if (flag == Flag.SFlag) {
            f = "SF";
        } else if (flag == Flag.PVFlag) {
            f = "PVF";
        } else if (flag == Flag.NFlag) {
            f = "NF";
        } else if (flag == Flag.B3Flag) {
            f = "B3F";
        } else if (flag == Flag.B5Flag) {
            f = "B5F";
        } else if (flag == Flag.HFlag) {
            f = "HF";
        } else {
            throw new RuntimeException();
        }
        return f + " = " + val + "; ";
    }

}
