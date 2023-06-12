package llj.asm.z80.exec;

import llj.asm.z80.*;

import java.util.ArrayList;
import java.util.List;

public class Z80SlowTemplate implements Z80Template {

    public static final int IX_PREFIX = 0xDD, IY_PREFIX = 0xFD;

    private int regA, regB, regC, regD, regE, regH, regL, regIXh, regIXl, regIYh, regIYl, wzh, wzl, datalatch, regSPh, regSPl;
    private int regA1, regB1, regC1, regD1, regE1, regH1, regL1;
    private int I, R;
    private int pc;

    private int tstates;

    private List<Integer> addressTrace = new ArrayList<>();
    private List<Integer> cmdTrace = new ArrayList<>();

    // Flags
    private boolean CF, ZF, SF, PVF, NF, B3F, B5F, HF;
    private boolean CF1, ZF1, SF1, PVF1, NF1, B3F1, B5F1, HF1;

    private int IFF, IM;
    boolean halted;

    public static interface Env {
        public byte getMemByte(int addr);
        public void setMemByte(int addr, int val);
        public void outToPort(int addr, int val);
        public int inFromPort(int addr);
    }
    private Env env;

    private static class SimpleEnv implements Env {
        private byte[] mem = new byte[65536];

        @Override
        public byte getMemByte(int addr) {
            return mem[addr];
        }

        @Override
        public void setMemByte(int addr, int val) {
            mem[addr] = (byte)val;
        }

        @Override
        public void outToPort(int addr, int val) {

        }

        @Override
        public int inFromPort(int addr) {
            return 0;
        }
    }

    public int fetchFromPC() {
        byte result = env.getMemByte(pc);
        pc++;
        return result & 0xFF;
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

    public void execSingle() {

        addressTrace.add(pc);

        int code = fetchFromPC();
        cmdTrace.add(code);
        tstates += 4;  // for fetching either prefix or opcode
        int prefix1 = -1, prefix2 = -1;
        if (code == IX_PREFIX) {
            prefix1 = code;
            code = fetchFromPC();
            cmdTrace.add(code);
            tstates += 4;  // for fetching opcode
        } else if (code == IY_PREFIX) {
            prefix1 = code;
            code = fetchFromPC();
            cmdTrace.add(code);
            tstates += 4;  // for fetching opcode
        } else if (code == 0xED) {
            prefix1 = code;
            code = fetchFromPC();
            cmdTrace.add(code);
            tstates += 4;  // for fetching opcode
        }

        if (code == 0xCB) {
            if (prefix1 != -1) {
                prefix2 = code;
                // for IX/IY bits/shifts, an order is following: IXIY_prefix(DD/FD), 0xCB_prefix, index_offset, opcode
                datalatch = fetchFromPC();
                tstates += 4;  // for fetching offset
                code = fetchFromPC();
                cmdTrace.add(code);
                tstates += 4;  // for fetching opcode
            } else {
                prefix1 = code;
                code = fetchFromPC();
                cmdTrace.add(code);
                tstates += 4;  // for fetching opcode
            }
        }

        int optmp = 0, op1full = 0, op2full = 0, opfull = 0; // for temp values
        boolean ftmp = false;

        // for easier templating the names of template methods should have no common prefixes
        if (prefix1 == -1) {
            singleOpcodeInstruction();
        } else if (prefix1 == IX_PREFIX) {
            dispatchIXInstruction(code, prefix1, prefix2);
        } else if (prefix1 == IY_PREFIX) {
            dispatchIYInstruction(code, prefix1, prefix2);
        } else if (prefix1 == 0xED) {
            edInstruction();
        } else if (prefix1 == 0xCB) {
            cbInstruction();
        }

    }

    public void dispatchIXInstruction(int code, int prefix1, int prefix2) {
        int optmp = 0, op1full = 0, op2full = 0, opfull = 0; // for temp values
        boolean ftmp = false;

        if (prefix2 == -1) {
            xInstruction();
        } else if (prefix2 == 0xCB) {
            qxBitsInstruction();
        }
    }

    public void dispatchIYInstruction(int code, int prefix1, int prefix2) {
        int optmp = 0, op1full = 0, op2full = 0, opfull = 0; // for temp values
        boolean ftmp = false;

        if (prefix2 == -1) {
            yInstruction();
        } else if (prefix2 == 0xCB) {
            wyBitsInstruction();
        }
    }


    public void singleOpcodeInstruction() {
        // empty stub
    }

    public void xInstruction() {
        // empty stub
    }

    public void qxBitsInstruction() {
        // empty stub
    }

    public void yInstruction() {
        // empty stub
    }

    public void wyBitsInstruction() {
        // empty stub
    }

    public void edInstruction() {
        // empty stub
    }

    public void cbInstruction() {
        // empty stub
    }

    // REG_A, REG_B, REG_C, REG_D, REG_E, REG_H, REG_L, REG_IXH, REG_IXL, REG_IYH, REG_IYL, WZH, WZL, PCL, PCH, DLATCH;
    public static String reg(InstructionExecAction.ExecOperand8 reg8) {
        switch (reg8) {
            case REG_A: return "regA";
            case REG_B: return "regB";
            case REG_C: return "regC";
            case REG_D: return "regD";
            case REG_E: return "regE";
            case REG_H: return "regH";
            case REG_L: return "regL";
            case REG_I: return "I";
            case REG_R: return "R";
            case REG_IXH: return "regIXh";
            case REG_IXL: return "regIXl";
            case REG_IYH: return "regIYh";
            case REG_IYL: return "regIYl";
            case WZH: return "wzh";
            case WZL: return "wzl";
            case REG_SPH: return "regSPh";
            case REG_SPL: return "regSPl";
            case DLATCH: return "datalatch";
            case PCL: return "0xFF & pc";
            case PCH: return "0xFF & (pc >> 8)";
            case FLAGS: return "packFlagByte()";
            default: throw new RuntimeException();
        }
    }

    @Override
    public String incAddr(InstructionExecAction.ExecOperand16 addr) {
        if (addr == InstructionExecAction.ExecOperand16.PC ) {
            return "pc++;";
        } else {
            return incdec16(IncDecInstruction.Operation.INC, addr);
        }
    }

    @Override
    public String decAddr(InstructionExecAction.ExecOperand16 addr) {
        if (addr == InstructionExecAction.ExecOperand16.PC ) {
            return "pc--;";
        } else {
            return incdec16(IncDecInstruction.Operation.DEC, addr);
        }
    }

    @Override
    public String setPC(int val) {
        return "pc = " + val + ";";
    }

    @Override
    public String setPC(InstructionExecAction.ExecOperand16 addr) {
        return "pc = " + getPairValue(InstructionExecAction.ExecOperand8.split(addr)) + ";";
    }

    @Override
    public String setPCRel(InstructionExecAction.ExecOperand8 offset) {
        return "pc = pc + (byte)(" + reg(offset) + ");";
    }

    @Override
    public String setPCRel(int offset) {
        return "pc = pc + (byte)(" + String.valueOf(offset) + ");";
    }

    @Override
    public String setAddrRel(InstructionExecAction.ExecOperand16 result, InstructionExecAction.ExecOperand16 base, InstructionExecAction.ExecOperand8 offset) {
        InstructionExecAction.ExecOperand8[] rpair = InstructionExecAction.ExecOperand8.split(result);
        String calc = "optmp = " + getAddr(base) + " + (byte)(" + reg(offset) + ");";
        String res = "" + reg(rpair[0]) +" = (optmp >> 8) & 0xFF; " + reg(rpair[1]) + " = (optmp & 0xFF);";
        return calc + res;
    }

    @Override
    public String memset8(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 val) {
        return "env.setMemByte(" + getAddr(addr)  + "," + reg(val) + "); ";
    }

    @Override
    public String memget8(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 receiver) {
        if (receiver == InstructionExecAction.ExecOperand8.FLAGS) {
            return "unpackFlagByte(0xFF & env.getMemByte(" + getAddr(addr) + "));";
        } else {
            return "" + reg(receiver) + "=env.getMemByte(" + getAddr(addr) + ") & 0xFF;";
        }
    }

    @Override
    public String portWrite(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 val) {
        if (val != null) {
            return "env.outToPort(" + getPairValue(InstructionExecAction.ExecOperand8.split(addr)) + "," + reg(val) + ");";
        } else {
            return "env.outToPort(" + getPairValue(InstructionExecAction.ExecOperand8.split(addr)) + ",0);";
        }
    }

    @Override
    public String portRead(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 receiver) {
        if (receiver != null) {
            return "" + reg(receiver) + "=env.inFromPort(" + getPairValue(InstructionExecAction.ExecOperand8.split(addr)) + ");";
        } else {
            return "env.inFromPort(" + getPairValue(InstructionExecAction.ExecOperand8.split(addr)) + ");";
        }
    }

    private static String getAddr(InstructionExecAction.ExecOperand16 addr) {
        return addr == InstructionExecAction.ExecOperand16.PC ? "pc" : getPairValue(InstructionExecAction.ExecOperand8.split(addr));
    }

    @Override
    public String reg8copy(InstructionExecAction.ExecOperand8 dest, InstructionExecAction.ExecOperand8 src) {
        return "" + reg(dest) + "=" + reg(src) + ";";
    }

    @Override
    public String reg16copy(InstructionExecAction.ExecOperand16 dest, InstructionExecAction.ExecOperand16 src) {
        InstructionExecAction.ExecOperand8[] destPair = InstructionExecAction.ExecOperand8.split(dest);
        InstructionExecAction.ExecOperand8[] srcPair = InstructionExecAction.ExecOperand8.split(src);
        return  ("" + reg(destPair[1]) + "=" + reg(srcPair[1]) + ";") + ("" + reg(destPair[0]) + "=" + reg(srcPair[0]) + ";");
    }

    @Override
    public String operation8(Op2Instruction.Operation operation, InstructionExecAction.ExecOperand8 op1, InstructionExecAction.ExecOperand8 op2) {
        String strOp;
        String op3 = ""; // "" means no additional operand

        String nf = "NF = false;";
        if (operation == Op2Instruction.Operation.ADD) {
            strOp = " + ";
        } else if (operation == Op2Instruction.Operation.ADC) {
            strOp = " + ";
            op3 = "(CF ? 1 : 0)";
        } else if (operation == Op2Instruction.Operation.SUB) {
            strOp = " - ";
            nf = "NF = true;";
        } else if (operation == Op2Instruction.Operation.SBC) {
            strOp = " - ";
            op3 = "(CF ? 1 : 0)";
            nf = "NF = true;";
        } else if (operation == Op2Instruction.Operation.AND) {
            strOp = " & ";
        } else if (operation == Op2Instruction.Operation.XOR) {
            strOp = " ^ ";
        } else if (operation == Op2Instruction.Operation.OR) {
            strOp = " | ";
        } else if (operation == Op2Instruction.Operation.CP) {
            strOp = " - ";
            nf = "NF = true;";
        } else {
            throw new RuntimeException();
        }

        String res = " optmp  = (" + reg(op1) + strOp + reg(op2) +  (op3.length() > 0 ? (strOp + op3) : "") + ");";
        String flags = flags8("optmp", reg(op1), reg(op2)) + nf;

        if (operation == Op2Instruction.Operation.CP) {
            // ignore result
            return res + flags;
        } else {
            // copy result into destination reg
            return res + flags + "" + reg(op1) + " = 0xFF & optmp;";
        }
    }


    public static String flags8(String res, String op1, String op2) {
        return " ZF = (0xFF & " + res + ") == 0; " + " CF = (" + res + " > 0xFF) || ( + " + res + "<0); SF = ((1 << 7) & " + res + ") > 0;" + "HF = ((" + op1 + " ^ " + op2 + " ^ " +  res + ") & 0x10) > 0;";

    }

    @Override
    public String incdec8(IncDecInstruction.Operation operation, InstructionExecAction.ExecOperand8 op, boolean dontUpdateFlags) {
        String strOp;
        String nf;
        if (operation == IncDecInstruction.Operation.INC) {
            strOp = " + 1";
            nf = "NF = false;";
        } else if (operation == IncDecInstruction.Operation.DEC) {
            strOp = " - 1 ";
            nf = "NF = true;";
        } else {
            throw new RuntimeException();
        }

        String res = " optmp  = (" + reg(op) + strOp + ");";
        String flags = dontUpdateFlags ? "" : (flags8sz("optmp") + ("HF = ((optmp ^ " + reg(op) + ") & 0x10) > 0;") + nf);

        // copy result into destination reg
        return res + flags + "" + reg(op) + " = 0xFF & optmp;";

    }

    public static String flags8sz(String val) {
        return " ZF = (0xFF & " + val + ") == 0; " + " SF = ((1 << 7) & " + val + ") > 0;";
    }

    public void daa() {
        int res = regA;
        if (NF) {
            if ((( regA & 0x0F) > 0x09) || HF) {
                res = res - 0x06;
            }
            if (( regA  > 0x99) || CF) {
                res = res - 0x60;
            }
        } else {
            if (((regA & 0x0F) > 0x09) || HF) {
                res = res + 0x06;
            }
            if ((regA > 0x99) || CF) {
                res = res + 0x60;
            }
        }
        if (res > 0x99) {
            CF = true;
        }

        ZF = (0xFF & res) == 0;
        SF = ((1 << 7) & res) > 0;
        HF = ((res ^ regA) & 0x10) > 0;

        regA = res;
    }

    @Override
    public String operation16(Op2Instruction.Operation operation, InstructionExecAction.ExecOperand16 dest, InstructionExecAction.ExecOperand16 src) {

        InstructionExecAction.ExecOperand8[] op1pair = InstructionExecAction.ExecOperand8.split(dest);
        InstructionExecAction.ExecOperand8[] op2pair = InstructionExecAction.ExecOperand8.split(src);

        // ( regH << 8 ) | regL
        String op1full = " op1full = " + getPairValue(op1pair) + ";";
        String op2full = " op2full = " + getPairValue(op2pair) + ";";

        String strOp;
        String op3 = ""; // "" means no additional operand
        String flags;
        if (operation == Op2Instruction.Operation.ADD) {
            strOp = " + ";
            flags = flagsADD16("optmp");
        } else if (operation == Op2Instruction.Operation.ADC) {
            strOp = " + ";
            op3 = "(CF ? 1 : 0)";
            flags = flagsADC16("optmp");
        } else if (operation == Op2Instruction.Operation.SUB) {
            throw new RuntimeException();
        } else if (operation == Op2Instruction.Operation.SBC) {
            strOp = " - ";
            op3 = "(CF ? 1 : 0)";
            flags = flagsADC16("optmp");
        } else {
            throw new RuntimeException();
        }

        String calc = " optmp  = ( op1full "  + strOp +  "op2full " +  (op3.length() > 0 ? (strOp + op3) : "") + ");";


        String result = "" + reg(op1pair[0]) +" = (optmp >> 8) & 0xFF; " + reg(op1pair[1]) + " = (optmp & 0xFF);";
        return op1full + op2full + calc + flags + result;

    }

    private static String getPairValue(InstructionExecAction.ExecOperand8[] op1pair) {
        return "((" + reg(op1pair[0]) + " << 8) | " + reg(op1pair[1]) + ")";
    }

    @Override
    public String incdec16(IncDecInstruction.Operation operation, InstructionExecAction.ExecOperand16 op) {

        InstructionExecAction.ExecOperand8[] oppair = InstructionExecAction.ExecOperand8.split(op);

        // ( regH << 8 ) | regL
        String opfull = " opfull = " + getPairValue(oppair) + ";";

        String action;
        if (operation == IncDecInstruction.Operation.INC) {
            action = " + 1";
        } else if (operation == IncDecInstruction.Operation.DEC) {
            action = " - 1 ";
        } else {
            throw new RuntimeException();
        }

        String calc = " optmp  = (" + "opfull" + action + ");";
        String result = "" + reg(oppair[0]) +" = (optmp >> 8) & 0xFF; " + reg(oppair[1]) + " = (optmp & 0xFF);";

        // copy result into destination reg
        return opfull + calc + result;

    }

    public static String flagsADC16(String val) {
        return " ZF = (0xFFFF & " + val + ") == 0; " + " CF = (" + val + " > 0xFFFF) ||( " + val + " < 0 );  SF = ((1 << 15) & " + val + ") > 0;";
    }

    public static String flagsADD16(String val) {
        return " CF = (" + val + " > 0xFFFF) ||( " + val + " < 0 );";
    }


    public String breakIfNotCond(ControlTransferInstruction.Condition condition) {
        String flag;
        if (condition.flag == ControlTransferInstruction.ConditionFlag.Z) {
            flag = "ZF";
        } else if (condition.flag == ControlTransferInstruction.ConditionFlag.C) {
            flag = "CF";
        } else if (condition.flag == ControlTransferInstruction.ConditionFlag.S) {
            flag = "SF";
        } else if (condition.flag == ControlTransferInstruction.ConditionFlag.PV) {
            flag = "PVF";
        } else {
            throw new RuntimeException();
        }

        if (condition.set) {
            return " if (!" + flag + ") break;";
        } else {
            return " if (" + flag + ") break;";
        }

    }

    @Override
    public String breakIfNotCond(InstructionExecAction.EndIfNotCondition.SpecialCondition condition) {
        String flag;
        if (condition == InstructionExecAction.EndIfNotCondition.SpecialCondition.BC_NZ) {
            String bc = getPairValue(InstructionExecAction.ExecOperand8.split(InstructionExecAction.ExecOperand16.REG_BC));
            flag = "(" + bc + ") == 0"; // we should break if condition NZ doesn't hold
        } else if (condition == InstructionExecAction.EndIfNotCondition.SpecialCondition.B_NZ) {
            flag = "(" + "regB" + ") == 0"; // we should break if condition NZ doesn't hold
        } else {
            throw new RuntimeException();
        }

        return " if (" + flag + ") break;";
    }

    public int packFlagByte() {
        return 0 | ((SF ? (1 << 7) : 0)) | ((ZF ? (1 << 6) : 0))  | ((B5F ? (1 << 5) : 0))  | ((HF ? (1 << 4) : 0))  | ((B3F ? (1 << 3) : 0)) | ((PVF ? (1 << 2) : 0)) | ((NF ? (1 << 1) : 0)) | (CF ? 1 : 0);
    }

    public void unpackFlagByte(int flags) {
        SF = (flags & (1 << 7)) > 0;
        ZF = (flags & (1 << 6)) > 0;
        B5F = (flags & (1 << 5)) > 0;
        HF = (flags & (1 << 4)) > 0;
        B3F = (flags & (1 << 3)) > 0;
        PVF = (flags & (1 << 2)) > 0;
        NF = (flags & (1 << 1)) > 0;
        CF = (flags & 1) > 0;
    }

    @Override
    public String shift(InstructionExecAction.ExecOperand8 operand, boolean left, boolean arithmetical) {

        String cf = "CF = (" + reg(operand) + "&" + (left ?  " 0x80 " : "0x1") + ") > 0 ;";
        String res;
        if (left) {
            if (arithmetical) {
                res = " optmp  = (" + reg(operand) + " << 1 );";
            } else {
                res = " optmp  = ((" + reg(operand) + " << 1) | 1 );";
            }
        } else {
            if (arithmetical) {
                res = " optmp  = ((" + reg(operand) + " >> 1) | (" + reg(operand) + " & 0x80) );";
            } else {
                res = " optmp  = (" + reg(operand) + " >> 1 );";
            }
        }

        String flags = flags8sz("optmp");

        // copy result into destination reg
        return cf + res + flags + "" + reg(operand) + " = 0xFF & optmp;";

    }

    @Override
    public String rotate(InstructionExecAction.ExecOperand8 operand, boolean left, boolean throughCarry, boolean shortForm) {

        String res;
        if (left) {
            if (throughCarry) {
                res = " optmp  = (" + reg(operand) + " << 1 ) | (CF ? 1 : 0);";
            } else {
                res = " optmp  = (" + reg(operand) + " << 1 ) | ((" + reg(operand) + " & 0x80) >> 7);";
            }
        } else {
            if (throughCarry) {
                res = " optmp  = (" + reg(operand) + " >> 1 ) | (CF ? 0x80 : 0);";
            } else {
                res = " optmp  = (" + reg(operand) + " >> 1 ) | ((" + reg(operand) + " & 0x1) << 7);";
            }
        }
        String cf = "CF = (" + reg(operand) + "&" + (left ?  " 0x80 " : "0x1") + ") > 0 ;";

        String flags = shortForm ? "" : flags8sz("optmp");

        // copy result into destination reg
        return res + cf + flags + "" + reg(operand) + " = 0xFF & optmp;";
    }

    @Override
    public String regAOp(RegAInstruction.Type type) {
        InstructionExecAction.ExecOperand8 op = InstructionExecAction.ExecOperand8.REG_A;
        if (type == RegAInstruction.Type.CPL) {
            return "" + reg(op) + "= (" + reg(op) + " ^ 0xFF) & 0xFF ;";
        } else if (type == RegAInstruction.Type.NEG) {
            return "" + reg(op) + "= ((1 << 16) - " + reg(op) + ") & 0xFF ;";
        } else if (type == RegAInstruction.Type.DAA) {
            return "daa();";
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public String ex16(InstructionExecAction.ExecOperand16 op1, InstructionExecAction.ExecOperand16 op2) {
        InstructionExecAction.ExecOperand8[] op1pair = InstructionExecAction.ExecOperand8.split(op1);
        InstructionExecAction.ExecOperand8[] op2pair = InstructionExecAction.ExecOperand8.split(op2);
        return    ex8(op2pair[1], op1pair[1]) + ex8(op2pair[0], op1pair[0]);

    }

    private static String ex8(InstructionExecAction.ExecOperand8 op2, InstructionExecAction.ExecOperand8 op1) {
        String r2 = reg(op2);
        String r1 = reg(op1);
        return ex8(r2, r1);
    }

    private static String ex8(String r2, String r1) {
        return ex(r2, r1, "optmp");
    }

    private static String ex(String r2, String r1, String tmpname) {
        return (" " + tmpname + "=" + r2 + ";") + ("" + r2 + "=" + r1 + ";") + ("" + r1 + "= " + tmpname + ";");
    }

    @Override
    public String exx() {
        return ex8("regB", "regB1") + ex8("regC", "regC1")  + ex8("regD", "regD1")  + ex8("regE", "regE1")  + ex8("regH", "regH1")  + ex8("regL", "regL1");
    }

    @Override
    public String exAF() {
        //     private boolean   B3F, B5F, HF;
        return ex8("regA", "regA1") + ex("CF", "CF1", "ftmp") + ex("ZF", "ZF1", "ftmp") + ex("SF", "SF1", "ftmp") + ex("PVF", "PVF1", "ftmp")    + ex("NF", "NF1", "ftmp") + ex("B3F", "B3F1", "ftmp") + ex("B5F", "B5F1", "ftmp") + ex("HF", "HF1", "ftmp");
    }

    @Override
    public String carryFlagOp(FlagInstruction.Type type) {
        if (type == FlagInstruction.Type.SCF) {
            return " CF = true;";
        } else if (type == FlagInstruction.Type.CCF) {
            return " CF = !CF;";
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public String bit(InstructionExecAction.ExecOperand8 operand, BitsInstruction.Type type, int bitIndex) {

        if (type == BitsInstruction.Type.SET) {
            return "" + reg(operand) + " = " + reg(operand) + "| (1 << " + String.valueOf(bitIndex) + ");";
        } else if (type == BitsInstruction.Type.RES) {
            return "" + reg(operand) + " = " + reg(operand) + "& (0xFF ^ (1 << " + String.valueOf(bitIndex) + "));";
        } else if (type == BitsInstruction.Type.CHECK) {
            return " ZF = (" + reg(operand) + "& (1 << " + String.valueOf(bitIndex) + ")) == 0;" + " SF = (" + reg(operand) + " & (1 << " + String.valueOf(bitIndex) + ") & 0x80) > 0;";
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public String complexRotate(InstructionExecAction.ExecOperand8 op1, InstructionExecAction.ExecOperand8 op2, ComplexRotateInstruction.Type type) {
        if (type == ComplexRotateInstruction.Type.RLD) {
            return "optmp = " + reg(op1) + "; " + reg(op1) + " = ((" + reg(op1) + " << 4) & 0xF0) | (0x0F & " + reg(op2) + "); " + reg(op2) + " = (optmp >> 4) & 0xF;" + flags8sz(reg(op2));
        } else if (type == ComplexRotateInstruction.Type.RRD) {
            return "optmp = " + reg(op1) + "; " + reg(op1) + " = ((" + reg(op1) + " >> 4) & 0x0F) | (0xF0 & (" + reg(op2) + " << 4)); " + reg(op2) + " = (optmp) & 0xF;"  + flags8sz(reg(op2));
        } else {
            throw new RuntimeException();
        }
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

    @Override
    public String im(String im) {
        return "im(" + im + ");";
    }

    @Override
    public String halt() {
        return "halted = true;";
    }

    @Override
    public String enableInts(boolean enable) {
        return "IFF = " + (enable ? "3" : "0") + ";";
    }

    public void reset() {
        halted = false;
        IFF = IM = 0;
        pc = 0;
        regSPh = 0xFF; regSPl = 0xFF;

        // af(SP);
        unpackFlagByte(64);

    }

    public void im(int i) {
        IM = i + 1; // TODO
    }

    boolean interrupt(int bus) {
        if((IFF&1)==0)
            return false;
        IFF = 0;
        halted = false;

        // tstates += 6;
        // push(PC);
        int opfull = ((regSPh << 8) | regSPl); int optmp  = (opfull - 1 );regSPh = (optmp >> 8) & 0xFF; regSPl = (optmp & 0xFF);env.setMemByte(((regSPh << 8) | regSPl),0xFF & (pc >> 8)); tstates += 3;  opfull = ((regSPh << 8) | regSPl); optmp  = (opfull - 1 );regSPh = (optmp >> 8) & 0xFF; regSPl = (optmp & 0xFF);env.setMemByte(((regSPh << 8) | regSPl),0xFF & pc); tstates += 3;

        switch(IM) {
            case 0:	// IM 0
            case 1:	// IM 0
                if((bus|0x38)==0xFF) {pc=bus-199; break;}
                /* not emulated */
            case 2:	// IM 1
                pc = 0x38; break;
            case 3:	// IM 2
                // pc = env.mem16(IR&0xFF00 | bus);
                tstates += 6;
                break;
        }
        // MP=PC;
        return true;
    }

    void nmi() {
        IFF &= 2;
        halted = false;

        // push(PC);
        int opfull = ((regSPh << 8) | regSPl); int optmp  = (opfull - 1 );regSPh = (optmp >> 8) & 0xFF; regSPl = (optmp & 0xFF);env.setMemByte(((regSPh << 8) | regSPl),0xFF & (pc >> 8)); tstates += 3;  opfull = ((regSPh << 8) | regSPl); optmp  = (opfull - 1 );regSPh = (optmp >> 8) & 0xFF; regSPl = (optmp & 0xFF);env.setMemByte(((regSPh << 8) | regSPl),0xFF & pc); tstates += 3;
        //tstates += 4;

        pc = 0x66;
    }


}
