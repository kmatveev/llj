package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class IntegerValue extends Value {

    public final int val;

    public IntegerValue(int val) {
        this.val = val;
    }

    @Override
    public TypeType getType() {
        return TypeType.INT;
    }

    @Override
    public int getSize() {
        return SIZE_SINGLE;
    }

    @Override
    public int getFirstWord() {
        return val;
    }

    @Override
    public int getSecondWord() {
        return 0;
    }

    public static IntegerValue load(OpaqueSingleSizeValue opaque) {
        return new IntegerValue(opaque.getFirstWord());
    }

    public static IntegerValue add(IntegerValue a, IntegerValue b) {
        return new IntegerValue(a.val + b.val);
    }

    public static IntegerValue sub(IntegerValue a, IntegerValue b) {
        return new IntegerValue(a.val - b.val);
    }

    public static IntegerValue inc(IntegerValue a) {
        return new IntegerValue(a.val + 1);
    }

    public static IntegerValue and(IntegerValue a, IntegerValue b) {
        return new IntegerValue(a.val & b.val);
    }

    public static IntegerValue or(IntegerValue a, IntegerValue b) {
        return new IntegerValue(a.val | b.val);
    }

    public static IntegerValue xor(IntegerValue a, IntegerValue b) {
        return new IntegerValue(a.val ^ b.val);
    }

    public static IntegerValue neg(IntegerValue a) {
        return new IntegerValue(~a.val);
    }

}
