package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class LongValue extends Value {

    public final long val;

    public LongValue(long val) {
        this.val = val;
    }

    @Override
    public TypeType getType() {
        return TypeType.LONG;
    }

    @Override
    public int getSize() {
        return SIZE_DOUBLE;
    }

    @Override
    public int getFirstWord() {
        return (int)val;
    }

    @Override
    public int getSecondWord() {
        return (int) (val >> 32);
    }

    public static LongValue load(OpaqueDoubleSizeValue val) {
        long v = val.getSecondWord();
        v = v << 32;
        v = v | val.getFirstWord();
        return new LongValue(v);
    }

    public static LongValue add(LongValue a, LongValue b) {
        return new LongValue(a.val + b.val);
    }

    public static LongValue sub(IntegerValue a, LongValue b) {
        return new LongValue(a.val - b.val);
    }


}
