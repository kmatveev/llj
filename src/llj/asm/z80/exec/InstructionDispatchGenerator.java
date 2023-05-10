package llj.asm.z80.exec;

import llj.asm.z80.*;

import java.util.ArrayList;
import java.util.List;

public class InstructionDispatchGenerator {

    private final Z80Template template;

    public InstructionDispatchGenerator(Z80Template template) {
        this.template = template;
    }

    public void generateSingleOpcodeInstructionDispatch(StringBuilder sb) {
        for (int i = 0; i < 256; i++) {

            if ((i == Instruction.IX_PREFIX ) || (i == Instruction.IY_PREFIX) || (i == 0xCB) || (i == 0xED)) {
                continue;
            }

            boolean found = false;
            boolean lastMatched = false;

            lastMatched = tryCallInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryDjnzInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryExInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryExxInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryFlagInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryIncDecInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryInterruptInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryIoInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryJumpInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryLdInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryNopInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryOp2Instruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryRegAInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryRetInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryRstInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryShiftRotateInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryStackInstruction(sb, i);
            found = updateFound(found, lastMatched);

            if (!found) {
                throw new RuntimeException();
            }

        }
    }

    private boolean updateFound(boolean found, boolean lastMatched) {
        if (found && lastMatched) {
            throw new RuntimeException();
        } else if (lastMatched) {
            found = true;
        }
        return found;
    }

    private boolean tryExInstruction(StringBuilder sb, int i) {
        ExInstruction instr = ExInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            String result;
            if (instr.op1.type == Instruction.Operand.Type.MEM_PTR_REG) {
                result = template.tmpset(template.reg16get(instr.op1.reg16));
                result += template.tmp3set(template.memget16(template.tmpget()));
                result += template.memset16(template.tmpget(), template.reg16get(instr.op2.reg16));
                result += template.reg16set(instr.op2.reg16, template.tmp3get());
            } else if (instr.op1.type == Instruction.Operand.Type.REG16) {
                Instruction.Operand.Reg8[] op2pair = Instruction.Operand.split(instr.op2.reg16);
                Instruction.Operand.Reg8[] op1pair = Instruction.Operand.split(instr.op1.reg16);

                result = template.tmp2set(template.reg8get(op1pair[0]));
                result += template.tmp3set(template.reg8get(op1pair[1]));
                result += template.reg8set(op1pair[0], template.reg8get(op2pair[0]));
                result += template.reg8set(op1pair[1], template.reg8get(op2pair[1]));
                result += template.reg8set(op2pair[0], template.tmp2get());
                result += template.reg8set(op2pair[1], template.tmp3get());
            } else {
                throw new RuntimeException();
            }
            sb.append(result);
            after(sb);
            return true;
        }
        return false;
    }

    private static StringBuilder after(StringBuilder sb) {
        return sb.append("break;").append("\n");
    }

    private static StringBuilder before(StringBuilder sb, int i) {
        return sb.append("case " + i + ":").append("\n");
    }

    private boolean tryDjnzInstruction(StringBuilder sb, int i) {
        DjnzInstruction djnz = DjnzInstruction.decodeInitial(i, -1);
        if (djnz != null) {
            before(sb, i);
            sb.append("// djnz;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryCallInstruction(StringBuilder sb, int i) {
        CallInstruction ci = CallInstruction.decodeInitial(i, -1);
        if (ci != null) {
            before(sb, i);
            sb.append("// call;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryExxInstruction(StringBuilder sb, int i) {
        ExxInstruction instr = ExxInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// exx;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryFlagInstruction(StringBuilder sb, int i) {
        FlagInstruction instr = FlagInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// flasg;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryIncDecInstruction(StringBuilder sb, int i) {
        IncDecInstruction instr = IncDecInstruction.decodeInitial(i, -1);
        if (instr != null) {

            Op2Instruction.Operation operation = instr.operation == IncDecInstruction.Operation.INC ? Op2Instruction.Operation.ADD : Op2Instruction.Operation.SUB;

            before(sb, i);
            String result;
            List<Integer> tstates = new ArrayList<>(3);
            if (instr.op.type == Instruction.Operand.Type.REG8) {
                String val = template.reg8get(instr.op.reg8);
                result = template.reg8set(instr.op.reg8, template.operation8(operation, val, "1"));
                // no additional tstates
            } else if (instr.op.type == Instruction.Operand.Type.REG16) {
                String val = template.reg16get(instr.op.reg16);
                result = template.reg16set(instr.op.reg16, template.operation16(operation, val, "1"));
                tstates.add(2);
            } else if (instr.op.type == Instruction.Operand.Type.MEM_PTR_REG) {
                String addr = template.reg16get(instr.op.reg16);
                if (instr.op.hasIndexOffset) {
                    addr += (" + " + template.imm8get());
                    tstates.add(8); // index mem read and index add
                }
                result = template.tmpset(addr);
                result += template.memset8(template.tmpget(), template.operation8(operation, template.memget8(template.tmpget()), "1"));
                tstates.add(3); // mem read 8
                tstates.add(1); // increment
                tstates.add(3); // mem write 8
            } else {
                throw new RuntimeException();
            }

            sb.append(result);
            writeUpdateTstates(sb, tstates);
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryInterruptInstruction(StringBuilder sb, int i) {
        InterruptInstruction instr = InterruptInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// interrupt;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryIoInstruction(StringBuilder sb, int i) {
        IoInstruction ci = IoInstruction.decodeInitial(i, -1);
        if (ci != null) {
            before(sb, i);
            sb.append("// io;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryJumpInstruction(StringBuilder sb, int i) {
        JumpInstruction instr = JumpInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// jump;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryLdInstruction(StringBuilder sb, int i) {
        LdInstruction instr = LdInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            String result;
            List<Integer> tstates = new ArrayList<>(5);
            if (instr.op1.type == Instruction.Operand.Type.REG8) {
                String val = getSrcVal(instr.op2, 1, tstates);
                result = template.reg8set(instr.op1.reg8, val);
                // no other tstates changes neeeded
            } else if (instr.op1.type == Instruction.Operand.Type.MEM_PTR_REG) {
                String val = getSrcVal(instr.op2, 1, tstates);
                String addr = template.reg16get(instr.op1.reg16);
                if (instr.op1.hasIndexOffset) {
                    addr += (" + " + template.imm8get());
                    tstates.add(8); // index mem read 8, then add
                }
                result = template.memset8(addr, val);
                tstates.add(3); // mem write 8
            } else if (instr.op1.type == Instruction.Operand.Type.REG16) {
                String val = getSrcVal(instr.op2, 2, tstates);
                result = template.reg16set(instr.op1.reg16, val);
                // no other tstates changes neeeded
            } else if (instr.op1.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
                String addr = template.imm16get();
                tstates.add(6); // imm16 get
                if (instr.op2.type == Instruction.Operand.Type.REG8) {
                    // we don't need full getSrcVal in this case
                    String val = template.reg8get(instr.op2.reg8);
                    result = template.memset8(addr, val);
                    tstates.add(3); // mem write 8
                } else if (instr.op2.type == Instruction.Operand.Type.REG16) {
                    result = template.tmp2set(addr);
                    // we don't need full getSrcVal in this case
                    String val = template.reg16get(instr.op2.reg16);
                    result += template.memset16(template.tmp2get(), val);
                    tstates.add(6); // mem write 16
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }

            // tstates validation, remove later
            {
                int r = 4; // for opcode fetch
                for (int rr : tstates) {
                    r += rr;
                }
                if (r != instr.tstates) {
                    throw new RuntimeException();
                }
            }

            sb.append(result);

            writeUpdateTstates(sb, tstates);

            after(sb);
            return true;
        }
        return false;
    }

    private static void writeUpdateTstates(StringBuilder sb, List<Integer> tstates) {
        if (tstates.size() == 0) return ;
        sb.append("tstates += ").append(tstates.get(0));
        for (int i = 1; i < tstates.size(); i++) {
            sb.append(" + " + tstates.get(i));
        }
        sb.append("; ");
    }

    private String getSrcVal(Instruction.Operand op, int size, List<Integer> tstates) {
        String val;
        if ((op.type == Instruction.Operand.Type.REG8) && (size ==1)){
            val = template.reg8get(op.reg8);
            // nothing to add to tstates queue, reg8-to-reg8 transfer is free
        } else if ((op.type == Instruction.Operand.Type.REG16) && (size == 2)){
            val = template.reg16get(op.reg16);
            tstates.add(2); // imply reg16-to-reg16 transfer
        } else if ((op.type == Instruction.Operand.Type.IMM8) && (size == 1 )) {
            val = template.imm8get();
            tstates.add(3); // imm8 get
        } else if ((op.type == Instruction.Operand.Type.IMM16) && (size == 2)) {
            val = template.imm16get();
            tstates.add(6); // imm16 get
        } else if ((op.type == Instruction.Operand.Type.MEM_PTR_REG) && (size == 1)) {
            String addr = template.reg16get(op.reg16);
            if (op.hasIndexOffset) {
                addr += (" + " + template.imm8get());
                tstates.add(8); // index mem read 8, then add
            }
            val = template.memget8(addr);
            tstates.add(3); // mem read 8
        } else if (op.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
            String addr = template.imm16get();
            tstates.add(6); // imm16 fetch
            if (size == 2) {
                val = template.memget16(addr);
                tstates.add(6); // mem read 16
            } else if (size == 1) {
                val = template.memget8(addr);
                tstates.add(3); // mem read 8
            } else {
                throw new RuntimeException();
            }

        } else {
            throw new RuntimeException();
        }
        return val;
    }

    private boolean tryNopInstruction(StringBuilder sb, int i) {
        NopInstruction ci = NopInstruction.decode(i, -1);
        if (ci != null) {
            before(sb, i);
            sb.append("// nop;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryOp2Instruction(StringBuilder sb, int i) {
        Op2Instruction instr = Op2Instruction.decodeInitial(i, -1);
        if (instr != null) {
            String result;
            before(sb, i);
            List<Integer> tstates = new ArrayList<>();
            if (instr.op1.type == Instruction.Operand.Type.REG8) {
                String val1 = template.reg8get(instr.op1.reg8);
                String val2 = getSrcVal(instr.op2, 1, tstates);
                result = template.tmp3set(template.operation8(instr.operation, val1, val2));
                result += template.reg8set(instr.op1.reg8, template.tmp3get());
            } else if (instr.op1.type == Instruction.Operand.Type.REG16) {
                String val1 = template.reg16get(instr.op1.reg16);
                String val2 = template.reg16get(instr.op2.reg16);
                result = template.operation16(instr.operation, val1, val2);
                tstates.add(7);
                result += template.reg16set(instr.op1.reg16, template.tmp3get());
                if ((instr.operation == Op2Instruction.Operation.AND) || (instr.operation == Op2Instruction.Operation.XOR) || (instr.operation == Op2Instruction.Operation.OR)) {
                    template.flagset(Z80Template.Flag.CFlag, "0");
                }
            } else {
                throw new RuntimeException();
            }
            sb.append(result);
            writeUpdateTstates(sb, tstates);
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRegAInstruction(StringBuilder sb, int i) {
        RegAInstruction instr = RegAInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// regA;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRetInstruction(StringBuilder sb, int i) {
        RetInstruction instr = RetInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// ret;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRstInstruction(StringBuilder sb, int i) {
        RstInstruction instr = RstInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// rst;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryShiftRotateInstruction(StringBuilder sb, int i) {
        ShiftRotateInstruction instr = ShiftRotateInstruction.decodeInitial(i, -1, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// shift;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryStackInstruction(StringBuilder sb, int i) {
        StackInstruction instr = StackInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            sb.append("// stack;").append("\n");
            after(sb);
            return true;
        }
        return false;
    }





}
