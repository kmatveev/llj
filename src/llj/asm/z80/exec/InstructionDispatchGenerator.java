package llj.asm.z80.exec;

import llj.asm.z80.*;

import java.util.List;

public class InstructionDispatchGenerator {

    private final Z80Template template;

    public InstructionDispatchGenerator(Z80Template template) {
        this.template = template;
    }

    public void generateSingleOpcodeInstructionDispatch(StringBuilder sb) {
        sb.append("switch(code) {");
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

            lastMatched = tryIncDecInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryInterruptInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryIoInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryJumpInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryLdInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryNopInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryOp2Instruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryRegAInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryRetInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryRstInstruction(sb, i);
            found = updateFound(found, lastMatched);

            lastMatched = tryShiftRotateInstruction(sb, i, -1, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryStackInstruction(sb, i, -1);
            found = updateFound(found, lastMatched);

            if (!found) {
                throw new RuntimeException();
            }

        }

        sb.append("}");
    }

    public void generateIndexDispatch(StringBuilder sb, int prefix1) {

        sb.append("switch(code) {");
        for (int i = 0; i < 256; i++) {

            boolean found = false;
            boolean lastMatched = false;

            lastMatched = tryLdInstruction(sb, i, prefix1);
            found = updateFound(found, lastMatched);

            lastMatched = tryOp2Instruction(sb, i, prefix1);
            found = updateFound(found, lastMatched);

            lastMatched = tryIncDecInstruction(sb, i, prefix1);
            found = updateFound(found, lastMatched);

            lastMatched = tryStackInstruction(sb, i, prefix1);
            found = updateFound(found, lastMatched);

            lastMatched = tryJumpInstruction(sb, i, prefix1);
            found = updateFound(found, lastMatched);

        }

        sb.append("default: break; \n }");

    }

    public void generateCBDispatch(StringBuilder sb) {
        sb.append("switch(code) {");
        for (int i = 0; i < 256; i++) {

            boolean found = false;
            boolean lastMatched = false;

            lastMatched = tryBitsInstruction(sb, i, 0xCB, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryShiftRotateInstruction(sb, i, 0xCB, -1);
            found = updateFound(found, lastMatched);

            if (!found) {
                throw new RuntimeException();
            }

        }

        sb.append("}");

    }

    public void generateIndexCBDispatch(StringBuilder sb, int prefix1) {
        sb.append("switch(code) {");
        for (int i = 0; i < 256; i++) {

            boolean found = false;
            boolean lastMatched = false;

            lastMatched = tryBitsInstruction(sb, i, prefix1, 0xCB);
            found = updateFound(found, lastMatched);

            lastMatched = tryShiftRotateInstruction(sb, i, prefix1, 0xCB);
            found = updateFound(found, lastMatched);

        }

        sb.append("default: break; \n }");

    }


    public void generateEDDispatch(StringBuilder sb) {
        sb.append("switch(code) {");
        for (int i = 0; i < 256; i++) {

            boolean found = false;
            boolean lastMatched = false;

            lastMatched = tryIoInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);

            lastMatched = tryRegAInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);

            lastMatched = tryLdInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);

            lastMatched = tryOp2Instruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);

            lastMatched = tryRetInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);

            lastMatched = tryBlockInstruction(sb, i, 0xED, -1);
            found = updateFound(found, lastMatched);

            lastMatched = tryInterruptInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);


            lastMatched = tryComplexRotateInstruction(sb, i, 0xED);
            found = updateFound(found, lastMatched);


        }

        sb.append("default: break; \n }");

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
//            if (instr.op1.type == Instruction.Operand.Type.MEM_PTR_REG) {
//                result = template.tmpset(template.reg16get(instr.op1.reg16));
//                result += template.tmp3set(template.memget16(template.tmpget()));
//                result += template.memset16(template.tmpget(), template.reg16get(instr.op2.reg16));
//                result += template.reg16set(instr.op2.reg16, template.tmp3get());
//            } else if (instr.op1.type == Instruction.Operand.Type.REG16) {
//                Instruction.Operand.Reg8[] op2pair = Instruction.Operand.split(instr.op2.reg16);
//                Instruction.Operand.Reg8[] op1pair = Instruction.Operand.split(instr.op1.reg16);
//
//                result = template.tmp2set(template.reg8get(op1pair[0]));
//                result += template.tmp3set(template.reg8get(op1pair[1]));
//                result += template.reg8set(op1pair[0], template.reg8get(op2pair[0]));
//                result += template.reg8set(op1pair[1], template.reg8get(op2pair[1]));
//                result += template.reg8set(op2pair[0], template.tmp2get());
//                result += template.reg8set(op2pair[1], template.tmp3get());
//            } else {
//                throw new RuntimeException();
//            }
//            sb.append(result);

            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private static StringBuilder after(StringBuilder sb) {
        return sb.append("break;").append("\n");
    }

    private static StringBuilder before(StringBuilder sb, int i) {
        return sb.append("case ").append(i).append(":").append("// 0x").append(Integer.toHexString(i)).append("\n");
    }

    private boolean tryDjnzInstruction(StringBuilder sb, int i) {
        DjnzInstruction djnz = DjnzInstruction.decodeInitial(i, -1);
        if (djnz != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(djnz);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryCallInstruction(StringBuilder sb, int i) {
        CallInstruction ci = CallInstruction.decodeInitial(i, -1);
        if (ci != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(ci);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryExxInstruction(StringBuilder sb, int i) {
        ExxInstruction instr = ExxInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryFlagInstruction(StringBuilder sb, int i) {
        FlagInstruction instr = FlagInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryIncDecInstruction(StringBuilder sb, int i, int prefix1) {
        IncDecInstruction instr = IncDecInstruction.decodeInitial(i, prefix1);
        if (instr != null) {

            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private static String getText(InstructionExecAction action, Z80Template template) {
        String val;
        if (action instanceof InstructionExecAction.Ld8) {
            InstructionExecAction.Ld8 ld8 = (InstructionExecAction.Ld8)action;
            val = template.reg8copy(ld8.dest, ld8.src);
        } else if (action instanceof InstructionExecAction.Ld16) {
            InstructionExecAction.Ld16 ld16 = (InstructionExecAction.Ld16)action;
            val = template.reg16copy(ld16.dest, ld16.src);
        } else if (action instanceof InstructionExecAction.MemGet) {
            InstructionExecAction.MemGet mem8get = (InstructionExecAction.MemGet)action;
            val = template.memget8(mem8get.addr, mem8get.dest);
            if (mem8get.postIncrementAddr) {
                val += template.incAddr(mem8get.addr);
            } else  if (mem8get.postDecrementAddr) {
                val += template.decAddr(mem8get.addr);
            }
        } else if (action instanceof InstructionExecAction.MemSet) {
            InstructionExecAction.MemSet mem8set = (InstructionExecAction.MemSet)action;
            val = "";
            if (mem8set.preDecrementAddr) {
                val += template.decAddr(mem8set.addr);
            }
            val += template.memset8(mem8set.addr, mem8set.dest);
            if (mem8set.postIncrementAddr) {
                val += template.incAddr(mem8set.addr);
            } else if (mem8set.postDecrementAddr) {
                val += template.decAddr(mem8set.addr);
            }
        } else if (action instanceof InstructionExecAction.Op8) {
            InstructionExecAction.Op8 op8 = (InstructionExecAction.Op8)action;
            val = template.operation8(op8.operation, op8.opDest, op8.opSrc);
        } else if (action instanceof InstructionExecAction.Op16) {
            InstructionExecAction.Op16 op16 = (InstructionExecAction.Op16)action;
            val = template.operation16(op16.operation, op16.opDest, op16.opSrc);
        } else if (action instanceof InstructionExecAction.IncDec8) {
            InstructionExecAction.IncDec8 incdec8 = (InstructionExecAction.IncDec8)action;
            val = template.incdec8(incdec8.operation, incdec8.operand, incdec8.dontUpdateFlags);
        } else if (action instanceof InstructionExecAction.IncDec16) {
            InstructionExecAction.IncDec16 incdec16 = (InstructionExecAction.IncDec16) action;
            val = template.incdec16(incdec16.operation, incdec16.operand);
        } else if (action instanceof InstructionExecAction.ShiftRot8) {
            InstructionExecAction.ShiftRot8 shiftRot8 = (InstructionExecAction.ShiftRot8) action;
            if (shiftRot8.type.rotate) {
                val = template.rotate(shiftRot8.op, shiftRot8.type.left, shiftRot8.type.throughCarry, shiftRot8.type.shortForm);
            } else {
                val = template.shift(shiftRot8.op, shiftRot8.type.left, shiftRot8.type.arithmetical);
            }
        } else if (action instanceof InstructionExecAction.Bits8) {
            InstructionExecAction.Bits8 bits8 = (InstructionExecAction.Bits8) action;
            val = template.bit(bits8.op, bits8.type, bits8.bitIndex);
        } else if (action instanceof InstructionExecAction.EndIfNotCondition) {
            InstructionExecAction.EndIfNotCondition cond = (InstructionExecAction.EndIfNotCondition)action;
            if (cond.condition != null) {
                val = template.breakIfNotCond(cond.condition);
            } else if (cond.special != null) {
                val = template.breakIfNotCond(cond.special);
            } else {
                val = ""; // for unconditional jumps/calls/rets
            }
        } else if (action instanceof InstructionExecAction.UpdatePCRel) {
            InstructionExecAction.UpdatePCRel updatePCRel = (InstructionExecAction.UpdatePCRel)action;
            if (updatePCRel.operand != null) {
                val = template.setPCRel(updatePCRel.operand);
            } else {
                val = template.setPCRel(updatePCRel.offset);
            }
        } else if (action instanceof InstructionExecAction.UpdatePCAbs) {
            InstructionExecAction.UpdatePCAbs updatePCAbs = (InstructionExecAction.UpdatePCAbs) action;
            if (updatePCAbs.operand != null) {
                val = template.setPC(updatePCAbs.operand);
            } else {
                val = template.setPC(updatePCAbs.absAddr);
            }
        } else if (action instanceof InstructionExecAction.RegA) {
            InstructionExecAction.RegA regAop = (InstructionExecAction.RegA) action;
            val = template.regAOp(regAop.type);
        } else if (action instanceof InstructionExecAction.CarryFlagAction) {
            InstructionExecAction.CarryFlagAction carryFlagAction = (InstructionExecAction.CarryFlagAction) action;
            val = template.carryFlagOp(carryFlagAction.type);
        } else if (action instanceof InstructionExecAction.ExxAction) {
            InstructionExecAction.ExxAction exxAction = (InstructionExecAction.ExxAction) action;
            if (exxAction.type == ExxInstruction.Type.EXX) {
                val = template.exx();
            } else if (exxAction.type == ExxInstruction.Type.EX_AF_AFF) {
                val = template.exAF();
            } else {
                throw new RuntimeException();
            }
        } else if (action instanceof InstructionExecAction.ExDEHL) {
            InstructionExecAction.ExDEHL ex = (InstructionExecAction.ExDEHL) action;
            val = template.ex16(InstructionExecAction.ExecOperand16.REG_DE, InstructionExecAction.ExecOperand16.REG_HL);
        } else if (action instanceof InstructionExecAction.GenericAction) {
            val = "";
        } else if (action instanceof InstructionExecAction.PortWrite) {
            InstructionExecAction.PortWrite portWrite = (InstructionExecAction.PortWrite)action;
            val = template.portWrite(portWrite.addr, portWrite.val);
        } else if (action instanceof InstructionExecAction.PortRead) {
            InstructionExecAction.PortRead portRead = (InstructionExecAction.PortRead) action;
            val = template.portRead(portRead.addr, portRead.dest);
        } else if (action instanceof InstructionExecAction.CalcIndexedAddr) {
            InstructionExecAction.CalcIndexedAddr calcIndexed = (InstructionExecAction.CalcIndexedAddr) action;
            val = template.setAddrRel(calcIndexed.dest, calcIndexed.src, calcIndexed.operand);
        } else if (action instanceof InstructionExecAction.ComplexRotateAction) {
            InstructionExecAction.ComplexRotateAction complexRotate = (InstructionExecAction.ComplexRotateAction)action;
            val = template.complexRotate(complexRotate.op1, complexRotate.op2, complexRotate.type);
        } else if (action instanceof InstructionExecAction.InterruptAction) {
            InstructionExecAction.InterruptAction intrAction = (InstructionExecAction.InterruptAction)action;
            if (intrAction.type == InterruptInstruction.Type.DI) {
                val = template.enableInts(false);
            } else if (intrAction.type == InterruptInstruction.Type.EI) {
                val = template.enableInts(true);
            } else if (intrAction.type == InterruptInstruction.Type.HALT) {
                val = template.halt();
            } else if (intrAction.type == InterruptInstruction.Type.IM) {
                val = template.im(String.valueOf(intrAction.im));
            } else {
                throw new RuntimeException();
            }
        } else {
            val = "Unknown";
        }


        return val;
    }

    private boolean tryInterruptInstruction(StringBuilder sb, int i, int prefix1) {
        InterruptInstruction instr = InterruptInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryIoInstruction(StringBuilder sb, int i, int prefix) {
        IoInstruction instr = IoInstruction.decodeInitial(i, prefix);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryJumpInstruction(StringBuilder sb, int i, int prefix1) {
        JumpInstruction instr = JumpInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryLdInstruction(StringBuilder sb, int i, int prefix1) {
        LdInstruction instr = LdInstruction.decodeInitial(i, prefix1);
        if (instr != null) {

            before(sb, i);

//            String result;
//            List<Integer> tstates = new ArrayList<>(5);
//            if (instr.opDest.type == Instruction.Operand.Type.REG8) {
//                String val = getSrcVal(instr.opSrc, 1, tstates);
//                result = template.reg8set(instr.opDest.reg8, val);
//                // no other tstates changes neeeded
//            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_REG) {
//                String val = getSrcVal(instr.opSrc, 1, tstates);
//                String addr = template.reg16get(instr.opDest.reg16);
//                if (instr.opDest.hasIndexOffset) {
//                    addr += (" + " + template.imm8get());
//                    tstates.add(8); // index mem read 8, then add
//                }
//                result = template.memset8(addr, val);
//                tstates.add(3); // mem write 8
//            } else if (instr.opDest.type == Instruction.Operand.Type.REG16) {
//                String val = getSrcVal(instr.opSrc, 2, tstates);
//                result = template.reg16set(instr.opDest.reg16, val);
//                // no other tstates changes neeeded
//            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
//                String addr = template.imm16get();
//                tstates.add(6); // imm16 get
//                if (instr.opSrc.type == Instruction.Operand.Type.REG8) {
//                    // we don't need full getSrcVal in this case
//                    String val = template.reg8get(instr.opSrc.reg8);
//                    result = template.memset8(addr, val);
//                    tstates.add(3); // mem write 8
//                } else if (instr.opSrc.type == Instruction.Operand.Type.REG16) {
//                    result = template.tmp2set(addr);
//                    // we don't need full getSrcVal in this case
//                    String val = template.reg16get(instr.opSrc.reg16);
//                    result += template.memset16(template.tmp2get(), val);
//                    tstates.add(6); // mem write 16
//                } else {
//                    throw new RuntimeException();
//                }
//            } else {
//                throw new RuntimeException();
//            }
//
//            // tstates validation, remove later
//            {
//                int r = 4; // for opcode fetch
//                for (int rr : tstates) {
//                    r += rr;
//                }
//                if (r != instr.tstates) {
//                    throw new RuntimeException();
//                }
//            }
//
//            sb.append(result);
//
//            writeUpdateTstates(sb, tstates);

            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;

        }
        return false;
    }

    private static void writeUpdateTstates(StringBuilder sb, int tstates) {
        if (tstates > 0) {
            sb.append("tstates += ").append(tstates).append("; ");
        }
    }

//    private String getSrcVal(Instruction.Operand op, int size, List<Integer> tstates) {
//        String val;
//        if ((op.type == Instruction.Operand.Type.REG8) && (size ==1)){
//            val = template.reg8get(op.reg8);
//            // nothing to add to tstates queue, reg8-to-reg8 transfer is free
//        } else if ((op.type == Instruction.Operand.Type.REG16) && (size == 2)){
//            val = template.reg16get(op.reg16);
//            tstates.add(2); // imply reg16-to-reg16 transfer
//        } else if ((op.type == Instruction.Operand.Type.IMM8) && (size == 1 )) {
//            val = template.imm8get();
//            tstates.add(3); // imm8 get
//        } else if ((op.type == Instruction.Operand.Type.IMM16) && (size == 2)) {
//            val = template.imm16get();
//            tstates.add(6); // imm16 get
//        } else if ((op.type == Instruction.Operand.Type.MEM_PTR_REG) && (size == 1)) {
//            String addr = template.reg16get(op.reg16);
//            if (op.hasIndexOffset) {
//                addr += (" + " + template.imm8get());
//                tstates.add(8); // index mem read 8, then add
//            }
//            val = template.memget8(addr);
//            tstates.add(3); // mem read 8
//        } else if (op.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
//            String addr = template.imm16get();
//            tstates.add(6); // imm16 fetch
//            if (size == 2) {
//                val = template.memget16(addr);
//                tstates.add(6); // mem read 16
//            } else if (size == 1) {
//                val = template.memget8(addr);
//                tstates.add(3); // mem read 8
//            } else {
//                throw new RuntimeException();
//            }
//
//        } else {
//            throw new RuntimeException();
//        }
//        return val;
//    }

    private boolean tryNopInstruction(StringBuilder sb, int i) {
        NopInstruction instr = NopInstruction.decode(i, -1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryOp2Instruction(StringBuilder sb, int i, int prefix1) {
        Op2Instruction instr = Op2Instruction.decodeInitial(i, prefix1);
        if (instr != null) {
            String result;
            before(sb, i);
//            List<Integer> tstates = new ArrayList<>();
//            if (instr.opDest.type == Instruction.Operand.Type.REG8) {
//                String val1 = template.reg8get(instr.opDest.reg8);
//                String val2 = getSrcVal(instr.opSrc, 1, tstates);
//                result = template.tmp3set(template.operation8(instr.operation, val1, val2));
//                result += template.reg8set(instr.opDest.reg8, template.tmp3get());
//            } else if (instr.opDest.type == Instruction.Operand.Type.REG16) {
//                String val1 = template.reg16get(instr.opDest.reg16);
//                String val2 = template.reg16get(instr.opSrc.reg16);
//                result = template.operation16(instr.operation, val1, val2);
//                tstates.add(7);
//                result += template.reg16set(instr.opDest.reg16, template.tmp3get());
//                if ((instr.operation == Op2Instruction.Operation.AND) || (instr.operation == Op2Instruction.Operation.XOR) || (instr.operation == Op2Instruction.Operation.OR)) {
//                    template.flagset(Z80Template.Flag.CFlag, "0");
//                }
//            } else {
//                throw new RuntimeException();
//            }
//            sb.append(result);
//            writeUpdateTstates(sb, tstates);

            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }

            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRegAInstruction(StringBuilder sb, int i, int prefix1) {
        RegAInstruction instr = RegAInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRetInstruction(StringBuilder sb, int i, int prefix1) {
        RetInstruction instr = RetInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryRstInstruction(StringBuilder sb, int i) {
        RstInstruction instr = RstInstruction.decodeInitial(i, -1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryShiftRotateInstruction(StringBuilder sb, int i, int prefix1, int prefix2) {
        ShiftRotateInstruction instr = ShiftRotateInstruction.decodeInitial(i, prefix1, prefix2);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryStackInstruction(StringBuilder sb, int i, int prefix) {
        StackInstruction instr = StackInstruction.decodeInitial(i, prefix);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryBitsInstruction(StringBuilder sb, int i, int prefix1, int prefix2) {
        BitsInstruction instr = BitsInstruction.decodeInitial(i, prefix1, prefix2);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryBlockInstruction(StringBuilder sb, int i, int prefix1, int prefix2) {
        BlockInstruction instr = BlockInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

    private boolean tryComplexRotateInstruction(StringBuilder sb, int i, int prefix1) {
        ComplexRotateInstruction instr = ComplexRotateInstruction.decodeInitial(i, prefix1);
        if (instr != null) {
            before(sb, i);
            List<InstructionExecAction> actions = InstructionExecAction.getFor(instr);
            for (InstructionExecAction action : actions) {
                sb.append(getText(action, template));
                writeUpdateTstates(sb, action.getTStates());
            }
            after(sb);
            return true;
        }
        return false;
    }

}
