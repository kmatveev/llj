package llj.asm.z80.exec;

import llj.asm.z80.*;

public interface Z80Template {

    String breakIfNotCond(InstructionExecAction.EndIfNotCondition.SpecialCondition condition);

    String shift(InstructionExecAction.ExecOperand8 operand, boolean left, boolean arithmetical);

    String rotate(InstructionExecAction.ExecOperand8 operand, boolean left, boolean throughCarry, boolean shortForm);

    String regAOp(RegAInstruction.Type type);

    String ex16(InstructionExecAction.ExecOperand16 op1, InstructionExecAction.ExecOperand16 op2);

    String exx();

    String exAF();

    String carryFlagOp(FlagInstruction.Type type);

    String bit(InstructionExecAction.ExecOperand8 operand, BitsInstruction.Type type, int bitIndex);

    String complexRotate(InstructionExecAction.ExecOperand8 op1, InstructionExecAction.ExecOperand8 op2, ComplexRotateInstruction.Type type);

    String flagset(Flag flag, String val);

    String im(String im);

    String halt();

    String enableInts(boolean enable);

    public static enum Flag {
        CFlag, ZFlag, SFlag, PVFlag, NFlag, B3Flag, B5Flag, HFlag;
    }

    String incAddr(InstructionExecAction.ExecOperand16 addr);

    String decAddr(InstructionExecAction.ExecOperand16 addr);

    String setPC(int val);

    String setPC(InstructionExecAction.ExecOperand16 addr);

    String setPCRel(InstructionExecAction.ExecOperand8 offset);

    String setPCRel(int offset);

    String setAddrRel(InstructionExecAction.ExecOperand16 result, InstructionExecAction.ExecOperand16 base, InstructionExecAction.ExecOperand8 offset);

    String memset8(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 val);

    String memget8(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 receiver);

    String portWrite(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 val);

    String portRead(InstructionExecAction.ExecOperand16 addr, InstructionExecAction.ExecOperand8 receiver);

    String reg8copy(InstructionExecAction.ExecOperand8 dest, InstructionExecAction.ExecOperand8 src);

    String reg16copy(InstructionExecAction.ExecOperand16 dest, InstructionExecAction.ExecOperand16 reg16);

    String operation8(Op2Instruction.Operation operation, InstructionExecAction.ExecOperand8 op1, InstructionExecAction.ExecOperand8 op2);

    String incdec8(IncDecInstruction.Operation operation, InstructionExecAction.ExecOperand8 op, boolean dontUpdateFlags);

    String operation16(Op2Instruction.Operation operation, InstructionExecAction.ExecOperand16 op1, InstructionExecAction.ExecOperand16 op2);

    String incdec16(IncDecInstruction.Operation operation, InstructionExecAction.ExecOperand16 op);

    String breakIfNotCond(ControlTransferInstruction.Condition condition);
}
