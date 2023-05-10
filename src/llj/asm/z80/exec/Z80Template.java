package llj.asm.z80.exec;

import llj.asm.z80.Instruction;
import llj.asm.z80.Op2Instruction;

public interface Z80Template {

    String flagset(Flag flag, String val);

    public static enum Flag {
        CFlag, ZFlag, SFlag, PVFlag, NFlag, B3Flag, B5Flag, HFlag;
    }

    String tmpget();

    String tmpset(String val);

    String memset8(String addr, String val);

    String memget8(String addr);

    String memget16(String addr);

    String memset16(String addr, String val);

    String reg8get(Instruction.Operand.Reg8 reg8);

    String tmp2get();

    String tmp2set(String val);

    String tmp3get();

    String tmp3set(String val);

    String reg8set(Instruction.Operand.Reg8 reg8, String val);

    String reg16get(Instruction.Operand.Reg16 reg16);

    String reg16set(Instruction.Operand.Reg16 reg16, String val);

    String imm8get();

    String imm16get();

    String operation8(Op2Instruction.Operation operation, String op1, String op2);

    String operation16(Op2Instruction.Operation operation, String op1, String op2);
}
