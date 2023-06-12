package llj.asm.z80.exec;

import llj.asm.z80.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static llj.asm.z80.Instruction.Operand.Reg16.REG_SP;

public abstract class InstructionExecAction {

    public enum ExecOperand8 {
        REG_A, REG_B, REG_C, REG_D, REG_E, REG_H, REG_L, REG_IXH, REG_IXL, REG_IYH, REG_IYL, WZH, WZL, PCL, PCH, REG_SPL, REG_SPH, DLATCH, FLAGS, REG_I, REG_R;

        public static ExecOperand8 forReg(Instruction.Operand.Reg8 reg) {
            switch (reg) {
                case REG_A:return REG_A;
                case REG_B:return REG_B;
                case REG_C:return REG_C;
                case REG_D:return REG_D;
                case REG_E:return REG_E;
                case REG_H:return REG_H;
                case REG_L:return REG_L;
                case REG_I:return REG_I;
                case REG_R:return REG_R;
                case REG_IXH:return REG_IXH;
                case REG_IXL:return REG_IXL;
                case REG_IYH:return REG_IYH;
                case REG_IYL:return REG_IYL;
                default: return null;
            }
        }

        public static ExecOperand8[] split(ExecOperand16 reg) {
            switch (reg) {
                case REG_BC:return new ExecOperand8[] {REG_B,   REG_C};
                case REG_DE:return new ExecOperand8[] { REG_D , REG_E };
                case REG_HL:return new ExecOperand8[] { REG_H , REG_L };
                case REG_IX:return new ExecOperand8[] { REG_IXH , REG_IXL };
                case REG_IY:return new ExecOperand8[] { REG_IYH , REG_IYL };
                case WZ:return new ExecOperand8[] { WZH , WZL };
                case REG_SP:return new ExecOperand8[] { REG_SPH , REG_SPL };
                case REG_AF:return new ExecOperand8[] { REG_A , FLAGS };
                default: return null;
            }
        }

    }

    public enum ExecOperand16 {
        REG_BC, REG_DE, REG_HL, REG_IX, REG_IY, PC, REG_SP, WZ, REG_AF;

        public static ExecOperand16 forReg(Instruction.Operand.Reg16 reg) {
            switch (reg) {
                case REG_BC:return REG_BC;
                case REG_DE:return REG_DE;
                case REG_HL:return REG_HL;
                case REG_IX:return REG_IX;
                case REG_IY:return REG_IY;
                case REG_SP:return REG_SP;
                case REG_AF:return REG_AF;
                default: return null;
            }
        }

    }

    public abstract int getTStates();

    public static List<InstructionExecAction> getFor(LdInstruction instr) {
        List<InstructionExecAction> result = new ArrayList<>(4);

        // instruction actions are split into 3 parts

        boolean special = false; // this flag should help to handle special case of LD (IX+d),n

        // part 1:
        // read offset byte, if needed
        if ((instr.opSrc.type == Instruction.Operand.Type.MEM_PTR_REG && instr.opSrc.hasIndexOffset) || (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_REG && instr.opDest.hasIndexOffset) ) {
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
        }
        // let's check if any operand is IMM so we need to read those IMM values from memory
        if (instr.opSrc.type == Instruction.Operand.Type.IMM8) {
            ExecOperand8 dest;
            if (instr.opDest.type == Instruction.Operand.Type.REG8) {
                // LD A,N
                dest = ExecOperand8.forReg(instr.opDest.reg8);
            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_REG) {
                // LD (HL),N
                dest = ExecOperand8.DLATCH;
                if (instr.opDest.hasIndexOffset) special = true;
            } else {
                throw new RuntimeException();
            }
            result.add(MemGet.withIncrement(ExecOperand16.PC, dest));
        } else if (instr.opSrc.type == Instruction.Operand.Type.IMM16) {
            // LD HL,NN
            if (instr.opDest.type != Instruction.Operand.Type.REG16) {
                throw new RuntimeException();
            }
            // on instruction semantics level we don't split SP into components
            // Instruction.Operand.Reg8[] pair = Instruction.Operand.split(instr.opDest.reg16);
            // so instead we do split on execution level
            ExecOperand8[] pair = ExecOperand8.split(ExecOperand16.forReg(instr.opDest.reg16));
            result.add(MemGet.withIncrement(ExecOperand16.PC, pair[1]));
            result.add(MemGet.withIncrement(ExecOperand16.PC, pair[0]));
        } else if (instr.opSrc.type == Instruction.Operand.Type.MEM_PTR_IMM16 || instr.opDest.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
            // LD A,(NN)
            // LD HL,(NN)
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZL));
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZH));
        }

        // part 2
        if (instr.opSrc.type == Instruction.Operand.Type.REG8) {
            if (instr.opDest.type == Instruction.Operand.Type.REG8) {
                // LD A,B
                result.add(new Ld8(ExecOperand8.forReg(instr.opSrc.reg8), ExecOperand8.forReg(instr.opDest.reg8)));
                // LD A,R
                if ((instr.opSrc.reg8 == Instruction.Operand.Reg8.REG_R) || (instr.opSrc.reg8 == Instruction.Operand.Reg8.REG_I) || (instr.opDest.reg8 == Instruction.Operand.Reg8.REG_R) || (instr.opDest.reg8 == Instruction.Operand.Reg8.REG_I)) {
                    result.add(new GenericAction(1));
                }
            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_REG ){
                // LD (HL),A
                // this will be handled in part 3
            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_IMM16 ){
                // LD (NN),A
                // this will be handled in part 3
            } else {
                throw new RuntimeException();
            }
        } else if (instr.opSrc.type == Instruction.Operand.Type.REG16) {
            if (instr.opDest.type == Instruction.Operand.Type.REG16) {
                // LD SP,HL
                result.add(new Ld16(ExecOperand16.forReg(instr.opSrc.reg16), ExecOperand16.forReg(instr.opDest.reg16)));
            } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
                // LD (NN),HL
                // this will be handled in part 3
            } else {
                throw new RuntimeException();
            }
        } else if (instr.opSrc.type == Instruction.Operand.Type.MEM_PTR_REG) {
            // LD A,(HL)
            // LD A,(IX+d)
            ExecOperand16 addr = ExecOperand16.forReg(instr.opSrc.reg16);
            if (instr.opSrc.hasIndexOffset) {
                result.add(new CalcIndexedAddr(addr, ExecOperand16.WZ, ExecOperand8.DLATCH));
                addr = ExecOperand16.WZ;
            }
            if (instr.opDest.type != Instruction.Operand.Type.REG8) {
                throw new RuntimeException();
            }
            result.add(new MemGet(addr, ExecOperand8.forReg(instr.opDest.reg8)));
        } else if (instr.opSrc.type == Instruction.Operand.Type.IMM8) {
            // LD A,N
            // LD (HL),N
            // LD (IX+d),N
            // nothing, operand was read on step 1
        } else if (instr.opSrc.type == Instruction.Operand.Type.IMM16) {
            // LD A,(NN)
            // LD HL,(NN)
            // nothing, operand was read on step 1
        } else if (instr.opSrc.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
            // LD A,(NN)
            // LD HL,(NN)
            // LD SP,(NN)
            // operand's address was read on step 1
            // we need to analyze destination to find if we need to fetch one or two bytes
            if (instr.opDest.type == Instruction.Operand.Type.REG8) {
                result.add(new MemGet(ExecOperand16.WZ, ExecOperand8.forReg(instr.opDest.reg8)));
            } else if (instr.opDest.type == Instruction.Operand.Type.REG16) {
                // on instruction semantics level we don't split SP into components
                // Instruction.Operand.Reg8[] pair = Instruction.Operand.split(instr.opDest.reg16);
                // so instead we do split on execution level
                ExecOperand8[] pair = ExecOperand8.split(ExecOperand16.forReg(instr.opDest.reg16));
                result.add(MemGet.withIncrement(ExecOperand16.WZ, pair[1]));
                result.add(new MemGet(ExecOperand16.WZ, pair[0]));
            } else {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }

        // part 3
        if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_REG) {
            // LD (HL),A
            // LD (HL),N
            ExecOperand16 dest = ExecOperand16.forReg(instr.opDest.reg16);
            if (instr.opDest.hasIndexOffset) {
                MemGet immReadAction = null;
                if (special) {
                    // a special case is if src is IMM8, in this case memread for imm8 is overlayed with this calculation of dest addr
                    immReadAction = (MemGet) result.remove(result.size() - 1);
                    immReadAction = new MemGet(immReadAction.addr, immReadAction.dest, immReadAction.postIncrementAddr, immReadAction.postDecrementAddr, true);
                }
                // add offset to index reg
                result.add(new CalcIndexedAddr(dest, ExecOperand16.WZ, ExecOperand8.DLATCH));
                dest = ExecOperand16.WZ;
                if (immReadAction != null) {
                    result.add(immReadAction);
                }
            }
            ExecOperand8 src;
            if (instr.opSrc.type == Instruction.Operand.Type.REG8) {
                // LD (HL),A
                src = ExecOperand8.forReg(instr.opSrc.reg8);
            } else if (instr.opSrc.type == Instruction.Operand.Type.IMM8) {
                // LD (HL),N
                src = ExecOperand8.DLATCH;
            } else {
                throw new RuntimeException();
            }
            result.add(new MemSet(dest, src));
        } else if (instr.opDest.type == Instruction.Operand.Type.MEM_PTR_IMM16) {
            // LD (NN),A
            // LD (NN),HL
            // operand's address was read on step 1
            // we need to analyze destination to find if we need to fetch one or two bytes
            if (instr.opSrc.type == Instruction.Operand.Type.REG8) {
                result.add(new MemSet(ExecOperand16.WZ, ExecOperand8.forReg(instr.opSrc.reg8)));
            } else if (instr.opSrc.type == Instruction.Operand.Type.REG16) {
                // on instruction semantics level we don't split SP into components
                // Instruction.Operand.Reg8[] pair = Instruction.Operand.split(instr.opDest.reg16);
                // so instead we do split on execution level
                ExecOperand8[] pair = ExecOperand8.split(ExecOperand16.forReg(instr.opSrc.reg16));
                result.add(MemSet.withIncrement(ExecOperand16.WZ, pair[1]));
                result.add(new MemSet(ExecOperand16.WZ, pair[0]));
            } else {
                throw new RuntimeException();
            }
        }

        return result;
    }

    public static List<InstructionExecAction> getFor(Op2Instruction instr) {
        List<InstructionExecAction> result = new ArrayList<>(4);

        // part 1: read IMM8 value, if needed
        ExecOperand8 src = null;
        if (instr.opSrc.type == Instruction.Operand.Type.IMM8) {
            // ADD A,N
            src = ExecOperand8.DLATCH;
            result.add(MemGet.withIncrement(ExecOperand16.PC, src));
        } else if (instr.opSrc.type == Instruction.Operand.Type.REG8) {
            // ADD A,B
            src = ExecOperand8.forReg(instr.opSrc.reg8);
        } else if ((instr.opSrc.type == Instruction.Operand.Type.MEM_PTR_REG)) {
            // ADD A,(HL)
            // ADD A,(IX+d)
            ExecOperand16 addr = ExecOperand16.forReg(instr.opSrc.reg16);
            if (instr.opSrc.hasIndexOffset) {
                // read offset
                result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
                // add offset to index reg
                if (instr.opSrc.hasIndexOffset) {
                    result.add(new CalcIndexedAddr(addr, ExecOperand16.WZ, ExecOperand8.DLATCH));
                    addr = ExecOperand16.WZ;
                }
            }
            src = ExecOperand8.DLATCH;
            result.add(new MemGet(addr, src));
        }

        // part 2: handling of operation
        if (instr.opDest.type == Instruction.Operand.Type.REG8) {
            if (src == null) throw new RuntimeException();
            result.add(new Op8(instr.operation, ExecOperand8.forReg(instr.opDest.reg8), src));
        } else if (instr.opDest.type == Instruction.Operand.Type.REG16) {
            result.add(new Op16(instr.operation, ExecOperand16.forReg(instr.opDest.reg16), ExecOperand16.forReg(instr.opSrc.reg16))); // this takes 7 tstates, and updates flags
        }

        return result;

    }

    public static List<InstructionExecAction> getFor(IncDecInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // part 1
        if ((instr.op.type == Instruction.Operand.Type.MEM_PTR_REG && instr.op.hasIndexOffset)) {
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
        }

        // part 2
        Op2Instruction.Operation op = instr.operation == IncDecInstruction.Operation.INC ? Op2Instruction.Operation.ADD : Op2Instruction.Operation.SUB;
        if (instr.op.type == Instruction.Operand.Type.REG8) {
            // INC A
            result.add(new IncDec8(instr.operation, ExecOperand8.forReg(instr.op.reg8), true)); // since destination is register, so it is overlapped with fetch of next instr
        } else if (instr.op.type == Instruction.Operand.Type.MEM_PTR_REG) {
            // INC (HL)
            // INC (IX+d)
            ExecOperand16 addr = ExecOperand16.forReg(instr.op.reg16);
            if (instr.op.hasIndexOffset) {
                // add offset to mem ptr reg
                result.add(new CalcIndexedAddr(addr, ExecOperand16.WZ, ExecOperand8.DLATCH));
                addr = ExecOperand16.WZ;
            }
            result.add(new MemGet(addr, ExecOperand8.DLATCH));
            result.add(new IncDec8(instr.operation, ExecOperand8.DLATCH, false)); // this takes 1 tstate, and updates flags
            result.add(new MemSet(addr, ExecOperand8.DLATCH)); // since destination is memory, so it is not overlapped
        } else if (instr.op.type == Instruction.Operand.Type.REG16) {
            // INC HL
            result.add(new IncDec16(instr.operation, ExecOperand16.forReg(instr.op.reg16)));  // this takes 2 tstates, and don't update flags
        }

        return result;
    }

    public static List<InstructionExecAction> getFor(JumpInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);


        if (instr.type == JumpInstruction.Type.RELATIVE) {
            // JR NN
            // JR Z,NN
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
            result.add(new EndIfNotCondition(instr.condition));
            result.add(new UpdatePCRel(ExecOperand8.DLATCH)); // this takes 5 tstates

        } else if (instr.type == JumpInstruction.Type.ABS_IMM) {
            // JP NN
            // https://retrocomputing.stackexchange.com/questions/22023/why-does-the-z80-jp-absolute-instruction-always-take-10-states-to-execute
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZL));
            result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZH));

            result.add(new EndIfNotCondition(instr.condition));
            result.add(new UpdatePCAbs(ExecOperand16.WZ));                  // 0 tstates

        } else if (instr.type == JumpInstruction.Type.ABS_REGISTER) {
            // JP HL
            result.add(new UpdatePCAbs(ExecOperand16.forReg(instr.reg16)));  // 0 tstates
        } else {
            throw new RuntimeException();
        }

        return result;
    }

    public static List<InstructionExecAction> getFor(DjnzInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // 1 extra tstate at this point
        result.add(new GenericAction(1));

        // part 1: read IMM value
        result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
        // this happens in parallel with imm read, so don't take any tstates
        result.add(new IncDec8(IncDecInstruction.Operation.DEC, ExecOperand8.REG_B, true, true));

        result.add(new EndIfNotCondition(EndIfNotCondition.SpecialCondition.B_NZ));
        result.add(new UpdatePCRel(ExecOperand8.DLATCH)); // this takes 5 tstates if condition is satisfied, and 0 if not

        return result;

    }

    public static List<InstructionExecAction> getFor(CallInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // read destination addr
        result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZL));
        result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZH));

        result.add(new EndIfNotCondition(instr.condition));
        result.addAll(Arrays.asList(push(ExecOperand8.PCL, ExecOperand8.PCH)));
        result.add(new UpdatePCAbs(ExecOperand16.WZ));                  // 0 tstates

        return result;

    }

    public static List<InstructionExecAction> getFor(RetInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        if (instr.condition != null) {
            result.add(new GenericAction(1));
            result.add(new EndIfNotCondition(instr.condition));
        }
        // get ret addr
        result.addAll(Arrays.asList(pop(ExecOperand8.WZL, ExecOperand8.WZH)));
        result.add(new UpdatePCAbs(ExecOperand16.WZ));                  // 0 tstates

        return result;

    }

    public static List<InstructionExecAction> getFor(RstInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        result.add(new GenericAction(1));
        result.addAll(Arrays.asList(push(ExecOperand8.PCL, ExecOperand8.PCH)));
        result.add(new UpdatePCAbs(instr.absAddr));                  // 0 tstates

        return result;

    }

    public static List<InstructionExecAction> getFor(StackInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // on instruction semantics level we don't split AF into components
        // Instruction.Operand.Reg8[] regs = Instruction.Operand.split(instr.reg16);
        // so instead we do split on execution level
        ExecOperand8[] pair = ExecOperand8.split(ExecOperand16.forReg(instr.reg16));

        if (instr.pushDirection) {
            result.addAll(Arrays.asList(push(pair[1], pair[0])));
        } else {
            result.addAll(Arrays.asList(pop(pair[1], pair[0])));
        }

        return result;

    }

    public static List<InstructionExecAction> getFor(ShiftRotateInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // part 1
        // for IX/IY shift instruction, an offset is coming before instruction opcode!
        // for example, SET 1,(IY+2) will be encoded as 0xFD, 0xCB, 0x02, 0xCE.
        // So we don't place dlatch action explicitly, it will be done during opcode fetch
        // if ((instr.op.type == Instruction.Operand.Type.MEM_PTR_REG && instr.op.hasIndexOffset)) {
        //    // read offset
        //    result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
        //}

        if (instr.op.type == Instruction.Operand.Type.REG8) {
            result.add(new ShiftRot8(ExecOperand8.forReg(instr.op.reg8), instr.type));
        } else if (instr.op.type == Instruction.Operand.Type.MEM_PTR_REG) {
            ExecOperand16 addr = ExecOperand16.forReg(instr.op.reg16);
            if (instr.op.hasIndexOffset) {
                // add offset to mem ptr reg
                result.add(new CalcIndexedAddr(addr, ExecOperand16.WZ, ExecOperand8.DLATCH));
                addr = ExecOperand16.WZ;
            }
            result.add(new MemGet(addr,ExecOperand8.DLATCH));
            result.add(new GenericAction(1)); // to account for 1 tstate taken by shif/trot
            result.add(new ShiftRot8(ExecOperand8.DLATCH, instr.type));
            result.add(new MemSet(addr,ExecOperand8.DLATCH));
        } else {
            throw new RuntimeException();
        }

        return result;
    }

    public static List<InstructionExecAction> getFor(BitsInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        // for IX/IY bits instruction, an offset is coming before instruction opcode!
        // for example, SET 1,(IY+2) will be encoded as 0xFD, 0xCB, 0x02, 0xCE.
        // So we don't place dlatch action explicitly, it will be done during opcode fetch
        // if ((instr.op.type == Instruction.Operand.Type.MEM_PTR_REG && instr.op.hasIndexOffset)) {
        //    result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.DLATCH));
        //}

        if (instr.op.type == Instruction.Operand.Type.REG8) {
            result.add(new Bits8(instr.type, instr.bitIndex, ExecOperand8.forReg(instr.op.reg8)));
        } else if (instr.op.type == Instruction.Operand.Type.MEM_PTR_REG) {
            ExecOperand16 addr = ExecOperand16.forReg(instr.op.reg16);
            if (instr.op.hasIndexOffset) {
                // add offset to mem ptr reg
                result.add(new CalcIndexedAddr(addr, ExecOperand16.WZ, ExecOperand8.DLATCH));
                addr = ExecOperand16.WZ;
            }
            result.add(new MemGet(addr,ExecOperand8.DLATCH));
            result.add(new Bits8(instr.type, instr.bitIndex, ExecOperand8.DLATCH));
            result.add(new GenericAction(1));
            if (instr.type != BitsInstruction.Type.CHECK) {
                result.add(new MemSet(addr, ExecOperand8.DLATCH));
            }
        } else {
            throw new RuntimeException();
        }

        return result;
    }

    public static List<InstructionExecAction> getFor(RegAInstruction instr) {
        return Collections.singletonList(new RegA(instr.type));
    }

    public static List<InstructionExecAction> getFor(NopInstruction instr) {
        return Collections.<InstructionExecAction>emptyList();
    }

    public static List<InstructionExecAction> getFor(IoInstruction instr) {

        List<InstructionExecAction> result = new ArrayList<>(4);

        if (instr.in) {
            if (instr.op2.type == Instruction.Operand.Type.PORT_NUM_IMM8) {
                result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZL));
                result.add(new Ld8(ExecOperand8.REG_A, ExecOperand8.WZH));
                result.add(new PortRead(ExecOperand16.WZ, ExecOperand8.forReg(instr.op1.reg8)));
            } else if (instr.op2.type == Instruction.Operand.Type.PORT_NUM_REG) {
                result.add(new PortRead(ExecOperand16.forReg(instr.op2.reg16), instr.op1.reg8 == null ? null : ExecOperand8.forReg(instr.op1.reg8)));
            } else {
                throw new RuntimeException();
            }
        } else {
            if (instr.op1.type == Instruction.Operand.Type.PORT_NUM_IMM8) {
                result.add(MemGet.withIncrement(ExecOperand16.PC, ExecOperand8.WZL));
                result.add(new Ld8(ExecOperand8.REG_A, ExecOperand8.WZH));
                result.add(new PortWrite(ExecOperand16.WZ, ExecOperand8.forReg(instr.op2.reg8)));
            } else if (instr.op1.type == Instruction.Operand.Type.PORT_NUM_REG) {
                result.add(new PortWrite(ExecOperand16.forReg(instr.op1.reg16), instr.op2.reg8 == null ? null : ExecOperand8.forReg(instr.op2.reg8)));
            } else {
                throw new RuntimeException();
            }
        }

        return result;

    }

    public static List<InstructionExecAction> getFor(ExInstruction instr) {

        if ((instr.op1.type == Instruction.Operand.Type.REG16) && (instr.op1.reg16 == Instruction.Operand.Reg16.REG_DE)) {
            // EX DE,HL
            return Collections.singletonList(new ExDEHL());
        } else if ((instr.op1.type == Instruction.Operand.Type.MEM_PTR_REG) && (instr.op1.reg16 == REG_SP)) {
            // EX (SP),HL
            List<InstructionExecAction> result = new ArrayList<>(4);
            result.addAll(Arrays.asList(pop(ExecOperand8.WZL, ExecOperand8.WZH)));
            result.addAll(Arrays.asList(push(ExecOperand8.REG_L, ExecOperand8.REG_H)));
            result.add(new Ld16(ExecOperand16.WZ, ExecOperand16.REG_HL));
            return result;
        } else {
            throw new RuntimeException();
        }
    }

    public static List<InstructionExecAction> getFor(FlagInstruction instr) {
        return Collections.singletonList(new CarryFlagAction(instr.type));
    }

    public static List<InstructionExecAction> getFor(ExxInstruction instr) {
        return Collections.singletonList(new ExxAction(instr.type));
    }

    public static List<InstructionExecAction> getFor(BlockInstruction instr) {
        List<InstructionExecAction> result = new ArrayList<>(4);
        if (instr.operation == BlockInstruction.Operation.LD) {
            if (instr.increment) {
                result.add(MemGet.withIncrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
                result.add(MemSet.withIncrement(ExecOperand16.REG_DE, ExecOperand8.DLATCH));
            } else {
                result.add(MemGet.withDecrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
                result.add(MemSet.withPostDecrement(ExecOperand16.REG_DE, ExecOperand8.DLATCH));
            }
            result.add(new IncDec16(IncDecInstruction.Operation.DEC, ExecOperand16.REG_BC));
            if (instr.repeated) {
                // TODO flags
                result.add(new EndIfNotCondition(EndIfNotCondition.SpecialCondition.BC_NZ));
                result.add(new UpdatePCRel(-2)); // this takes 5 tstates
            }
        } else if (instr.operation == BlockInstruction.Operation.CP) {
            if (instr.increment) {
                result.add(MemGet.withIncrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
            } else {
                result.add(MemGet.withDecrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
            }
            result.add(new Op8(Op2Instruction.Operation.CP, ExecOperand8.REG_A, ExecOperand8.DLATCH));
            result.add(new IncDec16(IncDecInstruction.Operation.DEC, ExecOperand16.REG_BC));
            if (instr.repeated) {
                // TODO flags
                result.add(new EndIfNotCondition(EndIfNotCondition.SpecialCondition.BC_NZ));
                result.add(new UpdatePCRel(-2)); // this takes 5 tstates
            }

        } else if (instr.operation == BlockInstruction.Operation.IN) {
            result.add(new GenericAction(1));
            result.add(new PortRead(ExecOperand16.REG_BC, ExecOperand8.DLATCH));
            result.add(new IncDec8(IncDecInstruction.Operation.DEC, ExecOperand8.REG_B, true));
            result.add(new GenericAction(1));
            result.add(MemSet.withIncrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
            if (instr.repeated) {
                // TODO  flags
                result.add(new EndIfNotCondition(EndIfNotCondition.SpecialCondition.BC_NZ));
                result.add(new UpdatePCRel(-2)); // this takes 5 tstates
            }

        } else if (instr.operation == BlockInstruction.Operation.OUT) {
            result.add(new GenericAction(1));
            result.add(MemGet.withIncrement(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
            result.add(new IncDec8(IncDecInstruction.Operation.DEC, ExecOperand8.REG_B, true));
            result.add(new PortWrite(ExecOperand16.REG_BC, ExecOperand8.DLATCH));
            result.add(new GenericAction(1));
            if (instr.repeated) {
                // TODO need to check flags
                result.add(new EndIfNotCondition(EndIfNotCondition.SpecialCondition.BC_NZ));
                result.add(new UpdatePCRel(-2)); // this takes 5 tstates
            }

        } else {
            throw new RuntimeException();
        }
        return result;
    }

    public static List<InstructionExecAction> getFor(InterruptInstruction instr) {
        return Collections.singletonList(new InterruptAction(instr.type, instr.im));
    }

    public static List<InstructionExecAction> getFor(ComplexRotateInstruction instr) {
        List<InstructionExecAction> result = new ArrayList<>(4);
        result.add(new MemGet(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
        result.add(new ComplexRotateAction(ExecOperand8.DLATCH, ExecOperand8.REG_A, instr.type));
        result.add(new MemSet(ExecOperand16.REG_HL, ExecOperand8.DLATCH));
        return result;
    }

    public static class GenericAction extends InstructionExecAction {

        public final int tstates;

        public GenericAction(int tstates) {
            this.tstates = tstates;
        }

        @Override
        public int getTStates() {
            return tstates;
        }
    }

    public static class Ld8 extends InstructionExecAction {

        public final ExecOperand8 src, dest;

        public Ld8(ExecOperand8 src, ExecOperand8 dest) {
            this.src = src;
            this.dest = dest;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class Ld16 extends InstructionExecAction {

        public final ExecOperand16 src, dest;

        public Ld16(ExecOperand16 src, ExecOperand16 dest) {
            this.src = src;
            this.dest = dest;
        }

        @Override
        public int getTStates() {
            return 2;
        }
    }

    public static class MemGet extends InstructionExecAction {

        public final ExecOperand16 addr;
        public final ExecOperand8 dest;
        public final boolean postIncrementAddr, postDecrementAddr;
        public final boolean overlapped;

        public MemGet(ExecOperand16 addr, ExecOperand8 dest) {
            this.addr = addr;
            this.dest = dest;
            postIncrementAddr = false;
            postDecrementAddr = false;
            overlapped = false;
        }

        public MemGet(ExecOperand16 addr, ExecOperand8 dest, boolean increment, boolean decrement, boolean overlapped) {
            this.addr = addr;
            this.dest = dest;
            postIncrementAddr = increment;
            postDecrementAddr = decrement;
            this.overlapped = overlapped;
        }

        public static MemGet withIncrement(ExecOperand16 addr, ExecOperand8 dest) {
            return new MemGet(addr, dest, true, false, false);
        }

        public static MemGet withIncrement(ExecOperand16 addr, ExecOperand8 dest, boolean overlapped) {
            return new MemGet(addr, dest, true, false, overlapped);
        }

        public static MemGet withDecrement(ExecOperand16 addr, ExecOperand8 dest) {
            return new MemGet(addr, dest, false, true, false);
        }

        @Override
        public int getTStates() {
            return overlapped ? 0 : 3;
        }
    }

    public static class MemSet extends InstructionExecAction {

        public final ExecOperand16 addr;
        public final ExecOperand8 dest;
        public final boolean postIncrementAddr, postDecrementAddr, preDecrementAddr;

        public MemSet(ExecOperand16 addr, ExecOperand8 dest) {
            this.addr = addr;
            this.dest = dest;
            postIncrementAddr = false;
            postDecrementAddr = false;
            preDecrementAddr = false;
        }

        public static MemSet withIncrement(ExecOperand16 addr, ExecOperand8 dest) {
            return new MemSet(addr, dest, true, false, false);
        }

        public static MemSet withPreDecrement(ExecOperand16 addr, ExecOperand8 dest) {
            return new MemSet(addr, dest, false, true, false);
        }

        public static MemSet withPostDecrement(ExecOperand16 addr, ExecOperand8 dest) {
            return new MemSet(addr, dest, false, false, true);
        }

        private MemSet(ExecOperand16 addr, ExecOperand8 dest, boolean postIncrement, boolean preDecrement, boolean postDecrement) {
            this.addr = addr;
            this.dest = dest;
            this.postIncrementAddr = postIncrement;
            this.preDecrementAddr = preDecrement;
            this.postDecrementAddr = postDecrement;
        }

        @Override
        public int getTStates() {
            return 3;
        }
    }

    public static class PortRead extends InstructionExecAction {

        public final ExecOperand16 addr;
        public final ExecOperand8 dest; // can be null

        public PortRead(ExecOperand16 addr, ExecOperand8 dest) {
            this.addr = addr;
            this.dest = dest;
        }

        @Override
        public int getTStates() {
            return 3;
        }
    }

    public static class PortWrite extends InstructionExecAction {

        public final ExecOperand16 addr;
        public final ExecOperand8 val; // can be null

        public PortWrite(ExecOperand16 addr, ExecOperand8 val) {
            this.addr = addr;
            this.val = val;
        }

        @Override
        public int getTStates() {
            return 3;
        }
    }

    public static class Op8 extends InstructionExecAction {

        public final ExecOperand8 opDest, opSrc;
        public final Op2Instruction.Operation operation;

        public Op8(Op2Instruction.Operation operation, ExecOperand8 opDest, ExecOperand8 opSrc) {
            this.operation = operation;
            this.opDest = opDest;
            this.opSrc = opSrc;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class IncDec8 extends InstructionExecAction {

        public final ExecOperand8 operand;
        public final IncDecInstruction.Operation operation;
        public final boolean overlapped;
        public final boolean dontUpdateFlags;

        public IncDec8(IncDecInstruction.Operation operation, ExecOperand8 operand) {
            this.operation = operation;
            this.operand = operand;
            this.overlapped = false;
            this.dontUpdateFlags = false;
        }

        public IncDec8(IncDecInstruction.Operation operation, ExecOperand8 operand, boolean overlapped) {
            this.operand = operand;
            this.operation = operation;
            this.overlapped = overlapped;
            this.dontUpdateFlags = false;
        }

        public IncDec8(IncDecInstruction.Operation operation, ExecOperand8 operand, boolean overlapped, boolean dontUpdateFlags) {
            this.operand = operand;
            this.operation = operation;
            this.overlapped = overlapped;
            this.dontUpdateFlags = dontUpdateFlags;
        }

        @Override
        public int getTStates() {
            return overlapped ? 0 : 1;
        }
    }

    public static class IncDec16 extends InstructionExecAction {

        public final ExecOperand16 operand;
        public final IncDecInstruction.Operation operation;

        public IncDec16(IncDecInstruction.Operation operation, ExecOperand16 operand) {
            this.operation = operation;
            this.operand = operand;
        }

        @Override
        public int getTStates() {
            return 2;
        }
    }



    public static class ShiftRot8 extends InstructionExecAction {

        public final ExecOperand8 op;
        public final ShiftRotateInstruction.Type type;

        public ShiftRot8(ExecOperand8 op, ShiftRotateInstruction.Type type) {
            this.op = op;
            this.type = type;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class Bits8 extends InstructionExecAction {

        public final BitsInstruction.Type type;
        public final int bitIndex;
        public final ExecOperand8 op;

        public Bits8(BitsInstruction.Type type, int bitIndex, ExecOperand8 op) {
            this.type = type;
            this.bitIndex = bitIndex;
            this.op = op;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class Op16 extends InstructionExecAction {

        public final ExecOperand16 opDest, opSrc;
        public final Op2Instruction.Operation operation;

        public Op16(Op2Instruction.Operation operation, ExecOperand16 opDest, ExecOperand16 opSrc) {
            this.operation = operation;
            this.opDest = opDest;
            this.opSrc = opSrc;
        }

        @Override
        public int getTStates() {
            return 7;
        }
    }

    public static class EndIfNotCondition extends InstructionExecAction {

        public final ControlTransferInstruction.Condition condition;
        public static enum SpecialCondition {BC_NZ, B_NZ}
        public final SpecialCondition special;

        public EndIfNotCondition(ControlTransferInstruction.Condition condition) {
            this.condition = condition;
            this.special = null;
        }

        public EndIfNotCondition(SpecialCondition condition) {
            this.condition = null;
            this.special = condition;
        }


        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class CalcIndexedAddr extends InstructionExecAction {

        public final ExecOperand16 src, dest;
        public final ExecOperand8 operand;
        public final int offset;

        public CalcIndexedAddr(ExecOperand16 src, ExecOperand16 dest, int offset) {
            this.src = src;
            this.dest = dest;
            this.operand = null;
            this.offset = offset;
        }

        public CalcIndexedAddr(ExecOperand16 src, ExecOperand16 dest, ExecOperand8 operand) {
            this.src = src;
            this.dest = dest;
            this.operand = operand;
            this.offset = 0;
        }

        @Override
        public int getTStates() {
            return 5;
        }
    }


    public static class UpdatePCRel extends InstructionExecAction {

        public final ExecOperand8 operand;
        public final int offset;

        public UpdatePCRel(ExecOperand8 operand) {
            this.operand = operand;
            offset = 0;
        }

        public UpdatePCRel(int absVal) {
            this.operand = null;
            this.offset = absVal;
        }

        @Override
        public int getTStates() {
            return 5;
        }
    }

    public static class UpdatePCAbs extends InstructionExecAction {

        public final ExecOperand16 operand;
        public final int absAddr;

        public UpdatePCAbs(ExecOperand16 operand) {
            this.operand = operand;
            absAddr = 0;
        }

        public UpdatePCAbs(int absAddr) {
            this.operand = null;
            this.absAddr = absAddr;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class RegA extends InstructionExecAction {

        public final RegAInstruction.Type type;

        public RegA(RegAInstruction.Type type) {
            this.type = type;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class CarryFlagAction extends InstructionExecAction {
        public FlagInstruction.Type type;

        public CarryFlagAction(FlagInstruction.Type type) {
            this.type = type;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class ExxAction extends InstructionExecAction {
        public ExxInstruction.Type type;

        public ExxAction(ExxInstruction.Type type) {
            this.type = type;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }


    public static class ExDEHL extends InstructionExecAction {
        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class InterruptAction extends InstructionExecAction {
        public final InterruptInstruction.Type type;
        public final int im;

        public InterruptAction(InterruptInstruction.Type type,int im) {
            this.type = type;
            this.im = im;
        }

        @Override
        public int getTStates() {
            return 0;
        }
    }

    public static class ComplexRotateAction extends InstructionExecAction {
        public final ExecOperand8 op1, op2;
        public final ComplexRotateInstruction.Type type;

        public ComplexRotateAction(ExecOperand8 op1, ExecOperand8 op2, ComplexRotateInstruction.Type type) {
            this.op1 = op1;
            this.op2 = op2;
            this.type = type;
        }

        @Override
        public int getTStates() {
            return 4;
        }
    }

    private static InstructionExecAction[] push(ExecOperand8 low, ExecOperand8 high) {
        InstructionExecAction[] actions = new InstructionExecAction[] {
                new GenericAction(1), // actual first decrement of SP happens here
                MemSet.withPreDecrement(ExecOperand16.REG_SP, high),
                MemSet.withPreDecrement(ExecOperand16.REG_SP, low)
        };
        return actions;
    }

    private static InstructionExecAction[] pop(ExecOperand8 low, ExecOperand8 high) {
        InstructionExecAction[] actions = new InstructionExecAction[] {
                MemGet.withIncrement(ExecOperand16.REG_SP, low),
                MemGet.withIncrement(ExecOperand16.REG_SP, high)
        };
        return actions;
    }

}
