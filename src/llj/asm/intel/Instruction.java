package llj.asm.intel;


import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedShort;

public interface Instruction {

    static Optional<MovInstruction> fromMachineCode(ByteBuffer bb, EnumSet<Prefix> prefixes) {
        boolean sizeOverride = prefixes.contains(Prefix.DATA_SIZE_OVERRIDE);
        short opCode = getUnsignedByte(bb);
        Operand op1, op2;
        if ((opCode == 0x8b) || (opCode == 0x8a)) {
            short modRM = getUnsignedByte(bb);
            int mod = (modRM >> 6) & 0b11;
            int regSizeBytes = (opCode == 0x8b) ? (sizeOverride ? 2 : 4) : 1;
            Operand.Register reg1 = MovInstruction.fromRegNum((modRM >> 3) & 0b111, regSizeBytes);
            op1 = new Operand.RegisterOp(reg1);
            Operand.Register reg2 = MovInstruction.fromRegNum(modRM & 0b111, regSizeBytes);
            if (mod == 0b11) {
                op2 = new Operand.RegisterOp(reg2);
            } else if (mod == 0b0) {
                op2 = new Operand.MemoryContentOp(Operand.Expression.register(reg2));
            } else {
                throw new RuntimeException();
            }
        } else if ((opCode >= 0xb0 ) && (opCode <= 0xb7)) {
            op1 = new Operand.RegisterOp(MovInstruction.fromRegNum(opCode - 0xb8, 1));
            op2 = Operand.ImmediateOp.fromConstant(getUnsignedByte(bb));
        } else if ((opCode >= 0xb8 ) && (opCode <= 0xbf)) {
            int regSizeBytes = (sizeOverride ? 2 : 4);
            op1 = new Operand.RegisterOp(MovInstruction.fromRegNum(opCode - 0xb8, regSizeBytes));
            int val = regSizeBytes == 2 ? getUnsignedShort(bb) : getInt(bb);
            op2 = Operand.ImmediateOp.fromConstant(val);
        } else {
            return Optional.empty();
        }
        return Optional.of(new MovInstruction(op1, op2));
    }

    void putMachineCode(ByteBuffer bb, LabelResolver resolver);

    public static enum MNEMONIC {
        MOV,
        JMP,
        CALL,
        RET,
        PUSH,
        POP,
        ADD,
        ADC,
        SUB,
        SBB,
        INC,
        DEC,
        CMP,
        LEA;
        
        public static Optional<MNEMONIC> get(String str) {
            try {
                return Optional.of(MNEMONIC.valueOf(str.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        
            
    }
    
    public static enum Prefix {
        LOCK(0xF0), REP(0xF3), REPNE(0xF2), SEG_CS(0x2E), SEG_SS(0x36), SEG_DS(0x3E), SEG_ES(0x26), SEG_FS(0x64), SEG_GS(0x65), DATA_SIZE_OVERRIDE(0x66), ADDR_SIZE_OVERRIDE(0x67);
        
        public final short code;

        Prefix(int code) {
            this.code = (short)code;
        }
    }
    
    public abstract MNEMONIC getMnemonic();
    
    public abstract int numOperands();
    
    public static Instruction newInstance(Instruction.MNEMONIC mnemonic, List<Operand> operands) {

            switch (mnemonic) {
                case MOV:
                    return MovInstruction.create(operands);
                case JMP:
                    return MovInstruction.create(operands);
                default:
                    throw new IllegalArgumentException();
            }
    }
    
}
