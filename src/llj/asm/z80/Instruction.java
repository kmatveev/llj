package llj.asm.z80;

public abstract class Instruction {

    public int opCode, prefix1 = -1, prefix2 = -1;

    public final static int BITS_PREFIX = 0xCB, EXT_PREFIX = 0xED, IX_PREFIX = 0xDD, IY_PREFIX = 0xFD;

    protected Instruction() {

    }

    public abstract String getMnemonic();

    public abstract int decodeRemaining(byte[] data, int offset);

    public abstract int getSize();

    protected int getOperationSize() {
        return 1 + ((prefix1 >= 0) ? 1 : 0) + ((prefix2 >= 0) ? 1 : 0);
    }

    public byte[] encode() {
        byte[] result = new byte[getSize()];
        encode(result, 0);
        return result;
    }

    public abstract int encode(byte[] dest, int offset);

    public static class Operand {

        public static enum Type { REG8, REG16, IMM8, IMM16, MEM_PTR_REG, MEM_PTR_IMM16, PORT_NUM_IMM8, PORT_NUM_REG }

        public static enum Reg8 {
            REG_A, REG_B, REG_C, REG_D, REG_E, REG_H, REG_L, REG_I, REG_R, REG_IXH, REG_IXL, REG_IYH, REG_IYL;
            public static Reg8 parse(String regArg) {
                for (Reg8 reg : values()) {
                    if (reg.name().substring(4).equals(regArg)) {
                        return reg;
                    }
                }
                return null;
            }
        }

        public static enum Reg16 {
            REG_AF, REG_BC, REG_DE, REG_HL, REG_IX, REG_IY, REG_SP;
            public static Reg16 parse(String regArg) {
                for (Reg16 reg : values()) {
                    if (reg.name().substring(4).equals(regArg)) {
                        return reg;
                    }
                }
                return null;
            }
        }

        private static final Reg8[] REG_B_C = {Reg8.REG_B, Reg8.REG_C};
        private static final Reg8[] REG_D_E = {Reg8.REG_D, Reg8.REG_E};
        private static final Reg8[] REG_H_L = {Reg8.REG_H, Reg8.REG_L};
        private static final Reg8[] REG_IXHL = {Reg8.REG_IXH, Reg8.REG_IXL};
        private static final Reg8[] REG_IYHL = {Reg8.REG_IYH, Reg8.REG_IYL};

        public static Reg8[] split(Reg16 reg16) {
            if (reg16 == Reg16.REG_BC) {
                return REG_B_C;
            } else if (reg16 == Reg16.REG_DE) {
                return REG_D_E;
            } else if (reg16 == Reg16.REG_HL) {
                return REG_H_L;
            } else if (reg16 == Reg16.REG_IX) {
                return REG_IXHL;
            } else if (reg16 == Reg16.REG_IY) {
                return REG_IYHL;
            } else {
                throw new RuntimeException();
            }
        }

        public Type type;
        public Reg8 reg8;
        public Reg16 reg16;
        public int imm;
        public int indexOffset;
        public boolean hasIndexOffset = false;

        private Operand(Type type, Reg8 reg8, Reg16 reg16, int imm, int indexOffset) {
            this.type = type;
            this.reg8 = reg8;
            this.reg16 = reg16;
            this.imm = imm;
            this.indexOffset = indexOffset;
        }

        public static Operand reg8(Reg8 reg8) {
            return new Operand(Type.REG8, reg8, null, 0, 0);
        }

        public static Operand reg16(Reg16 reg16) {
            return new Operand(Type.REG16, null, reg16, 0, 0);
        }

        public static Operand imm8(int imm8) {
            return new Operand(Type.IMM8, null, null, imm8, 0);
        }

        public static Operand imm16(int imm16) {
            return new Operand(Type.IMM16, null, null, imm16, 0);
        }

        public static Operand memRegPtr(Reg16 reg16) {
            return new Operand(Type.MEM_PTR_REG, null, reg16, 0, 0);
        }

        public static Operand memRegPtrIndex(Reg16 reg16, int offset) {
            Operand result = new Operand(Type.MEM_PTR_REG, null, reg16, 0, offset);
            result.hasIndexOffset = true;
            return result;
        }

        public static Operand memImmPtr(int imm16) {
            return new Operand(Type.MEM_PTR_IMM16, null, null, imm16, 0);
        }

        public static Operand port(int imm8) {
            return new Operand(Type.PORT_NUM_IMM8, null, null, imm8, 0);
        }

        public static Operand portReg(Reg16 reg16) {
            return new Operand(Type.PORT_NUM_REG, null, reg16, 0, 0);
        }


    }

    public static int decodeImm16(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    public static int encodeImm16(byte[] dest, int offset, int imm) {
        dest[offset] = (byte) (imm & 0xFF);
        offset++;
        dest[offset] = (byte) ((imm >> 8) & 0xFF);
        offset++;
        return offset;
    }

    protected int encodeOperation(byte[] dest, int offset) {
        if (prefix1 >= 0) {
            dest[offset] = (byte)prefix1;
            offset++;
        }
        if (prefix2 >= 0) {
            dest[offset] = (byte)prefix2;
            offset++;
        }
        dest[offset] = (byte) opCode;
        offset++;
        return offset;
    }

}
