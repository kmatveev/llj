package llj.asm.intel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static llj.asm.intel.Operand.Register.*;
import static llj.util.BinIOTools.getInt;
import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class MovInstruction implements Instruction {

    public static final int NUM_OPERANDS = 2;
    public Operand op1, op2;
    
    public MovInstruction(Operand op1, Operand op2) {
        if ((op1 instanceof Operand.MemoryContentOp) && (op2 instanceof Operand.MemoryContentOp)) {
            throw new IllegalArgumentException("MOV doesn't support mem-to-mem");
        }
        if ((op1 instanceof Operand.RegisterOp) && (op2 instanceof Operand.RegisterOp)) {
            if (((Operand.RegisterOp)op1).reg.numBytes != ((Operand.RegisterOp)op2).reg.numBytes) {
                throw new IllegalArgumentException("register sizes don't match");
            }
        }

        this.op1 = op1;
        this.op2 = op2;
    }

    public static MovInstruction create(List<Operand> operands) {
        if (operands.size() != NUM_OPERANDS) {
            throw new IllegalArgumentException("Incorrect number of operands:" + operands.size());
        }
        Operand op1 = operands.get(0), op2 = operands.get(1);
        return new MovInstruction(op1, op2);
    }

    @Override
    public MNEMONIC getMnemonic() {
        return Instruction.MNEMONIC.MOV;
    }

    @Override
    public int numOperands() {
        return NUM_OPERANDS;
    }
    
    @Override
    public void putMachineCode(ByteBuffer bb, LabelResolver resolver) {
        if (op1 instanceof Operand.RegisterOp) {

            Operand.Register reg1 = ((Operand.RegisterOp)op1).reg;
            if (op2 instanceof Operand.ImmediateOp) {
                Operand.ValOrLabel val = ((Operand.ImmediateOp) this.op2).val;
                val.maybeResolve(resolver, this);
                if (reg1.numBytes == 1) {
                    short opCode = (short) (0xb0 + regNum(reg1));
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, (byte)val.val);
                } else if (reg1.numBytes == 2) {
                    putUnsignedByte(bb, Prefix.DATA_SIZE_OVERRIDE.code);
                    short opCode = (short) (0xb8 + regNum(reg1));
                    putUnsignedByte(bb, opCode);
                    putUnsignedShort(bb, val.val, ByteOrder.LITTLE_ENDIAN);
                } else if (reg1.numBytes == 4) {
                    short opCode = (short) (0xb8 + regNum(reg1));
                    putUnsignedByte(bb, opCode);
                    putUnsignedInt(bb, val.val, ByteOrder.LITTLE_ENDIAN);
                } else {
                    throw new RuntimeException();
                }
            } else if (op2 instanceof Operand.RegisterOp) {
                Operand.Register reg2 = ((Operand.RegisterOp)op2).reg;
                short modRM = (short) ((0b11 << 6) | (regNum(reg1) << 3) | regNum(reg2));
                if (reg1.numBytes == 1) {
                    short opCode = 0x8a; // could be also 0x88 opcode in this case
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg1.numBytes == 2) {
                    putUnsignedByte(bb, Prefix.DATA_SIZE_OVERRIDE.code);
                    short opCode = 0x8b; // could be also 0x89 opcode in this case
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg1.numBytes == 4) {
                    short opCode = 0x8b; // could be also 0x89 opcode in this case
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else {
                    throw new RuntimeException();
                }
            } else if (op2 instanceof Operand.MemoryContentOp) {
                Operand.Expression expr = ((Operand.MemoryContentOp)op2).expr;
                short modRM;
                boolean addrSizePrefix = false;
                if (expr.register != null) {
                    if (expr.register.numBytes == 2) {
                        addrSizePrefix = true;
                        modRM = getModRM16(reg1, expr);

                    } else if (expr.register.numBytes == 1) {
                        throw new IllegalArgumentException(); // 8-bit registers are not supported 
                    } else if (expr.register.numBytes == 4) {
                        // TODO
                        throw new RuntimeException();
                    } else {
                        throw new RuntimeException();
                    }
                } else if (expr.valOrLabel != null) {
                    modRM = (short)((0b00 << 6) | (regNum(reg1) << 3) | 0b110);
                    // TODO supply machine code for address, and update addSizePrefix if needed
                } else {
                    throw new RuntimeException(); // either addr or register should be present
                }

                if (reg1.numBytes == 1) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    short opCode = 0x8a;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg1.numBytes == 2) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    putUnsignedByte(bb, Prefix.DATA_SIZE_OVERRIDE.code);
                    short opCode = 0x8b;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg1.numBytes == 4) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    short opCode = 0x8b;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else {
                    throw new RuntimeException();
                }
                

            } else {
                throw new RuntimeException(); // should never happen, since all possible operand types have been processed
            }
        } else if (op1 instanceof Operand.MemoryContentOp) {
            if (op2 instanceof Operand.RegisterOp) {
                Operand.Register reg2 = ((Operand.RegisterOp)op2).reg;
                
                Operand.Expression expr = ((Operand.MemoryContentOp) op1).expr;
                short modRM;
                boolean addrSizePrefix = false;
                if (expr.register != null) {
                    if (expr.register.numBytes == 2) {
                        addrSizePrefix = true;
                        modRM = getModRM16(reg2, expr);
                    } else if (expr.register.numBytes == 1) {
                        throw new IllegalArgumentException(); // 8-bit registers are not supported 
                    } else if (expr.register.numBytes == 4) {
                        // TODO
                        throw new RuntimeException();
                    } else {
                        throw new RuntimeException();
                    }
                } else if (expr.valOrLabel != null) {
                    modRM = (short) ((0b00 << 6) | (regNum(reg2) << 3) | 0b110);
                    // TODO supply machine code for address, and update addSizePrefix if needed
                } else {
                    throw new RuntimeException(); // either addr or register should be present
                }

                if (reg2.numBytes == 1) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    short opCode = 0x88;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg2.numBytes == 2) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    short opCode = 0x89;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else if (reg2.numBytes == 4) {
                    if (addrSizePrefix) {
                        putUnsignedByte(bb, Prefix.ADDR_SIZE_OVERRIDE.code);
                    }
                    short opCode = 0x89;
                    putUnsignedByte(bb, opCode);
                    putUnsignedByte(bb, modRM);
                } else {
                    throw new RuntimeException();
                }
                
            } else if (op2 instanceof Operand.ImmediateOp) {
                // TODO
            }
            
        } else {
            throw new IllegalArgumentException();
        }
        return;
    }

    public static short getModRM16(Operand.Register reg1, Operand.Expression expr) {
        // expr.register1 is expected to be 
        
        short modRM;
        if (expr.register2 != null) {
            // only some combinations of specific 16-bit registers is supported by machine code
            if (((expr.register == Operand.Register.BX) && (expr.register2 == Operand.Register.SI)) || ((expr.register == Operand.Register.SI) && (expr.register2 == Operand.Register.BX))) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b000);
            } else if (((expr.register == Operand.Register.BX) && (expr.register2 == Operand.Register.DI)) || ((expr.register == Operand.Register.DI) && (expr.register2 == Operand.Register.BX))) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b001);
            } else if (((expr.register == Operand.Register.BP) && (expr.register2 == Operand.Register.SI)) || ((expr.register == Operand.Register.SI) && (expr.register2 == Operand.Register.BP))) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b010);
            } else if (((expr.register == Operand.Register.BP) && (expr.register2 == Operand.Register.DI)) || ((expr.register == Operand.Register.DI) && (expr.register2 == Operand.Register.BP))) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b011);
            } else {
                throw new IllegalArgumentException();// [bx+bp] is not supported
            }
            if (expr.valOrLabel != null) {
                // TODO override mod part, and provide machine code bytes representing offset 
            }

        } else {

            // only some 16-bit registers is supported by machine code
            if (expr.register == Operand.Register.BX) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b111);
            }  else if (expr.register == Operand.Register.SI) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b100);
            }  else if (expr.register == Operand.Register.DI) {
                modRM =  (short) ((0b00 << 6) | (regNum(reg1) << 3) | 0b101);
            } else {
                throw new IllegalArgumentException(); // [ax] is not supported 
            }

        }
        return modRM;
    }

    public static int regNum(Operand.Register reg) {
        switch(reg) {
            case AL:
            case AX:
            case EAX: return 0;
            case CX:
            case ECX: return 1;
            case DX:
            case EDX: return 2;
            case BL:
            case BX:
            case EBX: return 3;
            case AH:
            case SP:
            case ESP: return 4;
            case CH:
            case BP:
            case EBP: return 5;
            case DH:
            case SI:
            case ESI: return 6;
            case BH:
            case DI:
            case EDI: return 7;
            default: throw new IllegalArgumentException();
        }
        
    }

    public static Operand.Register fromRegNum(int regNum, int regSizeBytes) {
        switch (regSizeBytes) {
            case 1:
                switch (regNum) {
                    case 0: return AL;
                    case 1: return CL;
                    case 2: return DL;
                    case 3: return BL;
                    case 4: return AH;
                    case 5: return CH;
                    case 6: return DH;
                    case 7: return BH;
                    default:
                        throw new IllegalArgumentException();
                }
            
            case 2:
                switch (regNum) {
                    case 0: return AX;
                    case 1: return CX;
                    case 2: return DX;
                    case 3: return BX;
                    case 4: return SP;
                    case 5: return BP;
                    case 6: return SI;
                    case 7: return DI;
                    default:
                        throw new IllegalArgumentException();
                }
                
            case 4:
                switch (regNum) {
                    case 0: return EAX;
                    case 1: return ECX;
                    case 2: return EDX;
                    case 3: return EBX;
                    case 4: return ESP;
                    case 5: return EBP;
                    case 6: return ESI;
                    case 7: return EDI;
                    default:
                        throw new IllegalArgumentException();
                }
            default: throw new IllegalArgumentException();    
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MovInstruction)) return false;
        MovInstruction another = (MovInstruction)obj;
        return this.op1.equals(another.op1) && this.op2.equals(another.op2);
    }
}
